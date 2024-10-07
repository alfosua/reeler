import { getFilenameByTimestamp } from '../utils'

export async function fetchYoutubeVideoInfo(url: string) {
  const raw = await getRawData(url)
  const query: YoutubePlayerResponseQueryData = raw.playerResponse

  const authorAvatarUrl = query.endscreen.endscreenRenderer.elements.find(
    (e) => e.endscreenElementRenderer.style === 'CHANNEL',
  )?.endscreenElementRenderer.image.thumbnails[0].url

  const result: VideoInfo = {
    filename: getFilenameByTimestamp('mp4'),
    sourceUrl: url,
    source: 'youtube',
    contentUrl: query.streamingData.adaptiveFormats[0].url,
    caption: query.videoDetails.title,
    duration: Number(query.videoDetails.lengthSeconds),
    username: query.videoDetails.author,
    thumbnailUrl: query.videoDetails.thumbnail.thumbnails[0].url,
    width: query.streamingData.adaptiveFormats[0].width,
    height: query.streamingData.adaptiveFormats[0].height,
    userAvatarUrl: authorAvatarUrl,
    raw,
  }

  return result
}

export const cachedTokens: Map<string, string[]> = new Map()

async function getRawData(url: string) {
  const response = await fetch(url)
  const html = await response.text()
  const rawDataString = html
    .split('var ytInitialPlayerResponse = ')[1]
    .split(';</script>')[0]

  const playerResponse = JSON.parse(rawDataString)

  return {
    playerResponse,
  }
}

type YoutubePlayerResponseQueryData = {
  streamingData: {
    adaptiveFormats: {
      itag: number
      url: string
      mimeType: string
      width: number
      height: number
      bitrate: number
      quality: string
      fps: number
    }[]
  }
  videoDetails: {
    title: string
    lengthSeconds: string
    thumbnail: {
      thumbnails: {
        url: string
        width: number
        height: number
      }[]
    }
    author: string
  }
  endscreen: {
    endscreenRenderer: {
      elements: {
        endscreenElementRenderer: {
          style: string
          image: {
            thumbnails: {
              url: string
              width: number
              height: number
            }[]
          }
        }
      }[]
    }
  }
}
