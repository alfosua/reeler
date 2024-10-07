import { getFilenameByTimestamp } from '../utils'
import * as cheerio from 'cheerio'

export async function fetchYoutubeVideoInfo(url: string) {
  const raw = await getRawData(url)
  const query: YoutubePlayerResponseQueryData = raw.playerResponse

  const authorAvatarUrl = query.endscreen.endscreenRenderer.elements.find(
    (e) => e.endscreenElementRenderer.style === 'CHANNEL',
  )?.endscreenElementRenderer.image.thumbnails[0].url

  const downloadUrl = await getDownloadUrl(url)

  const result: VideoInfo = {
    filename: getFilenameByTimestamp('mp4'),
    sourceUrl: url,
    source: 'youtube',
    contentUrl: downloadUrl || '',
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

async function getDownloadUrl(url: string) {
  const response = await fetch('https://shortsnoob.com/en')
  const html = await response.text()
  const $ = cheerio.load(html)
  const setCookie = response.headers.getSetCookie()
  const xsrfToken = setCookie[0].split('; ')[0]
  const session = setCookie[1].split('; ')[0]
  const cookie = [xsrfToken, session].join('; ')
  const token = $('input[name="_token"]').attr('value')

  const data = new URLSearchParams()
  data.append('_token', token || '')
  data.append('link', url)
  data.append('locale', 'en')
  const body = data.toString()

  const searchResponse = await fetch('https://shortsnoob.com/', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'Content-Length': body.length.toString(),
      Cookie: cookie || '',
    },
    body: body,
  })

  const searchHtml = await searchResponse.text()
  const $search = cheerio.load(searchHtml)

  const redirectUrlHref = $search(
    'a[href^="https://shortsnoob.com/redirect"]',
  )?.attr('href')
  const redirectUrl = new URL(redirectUrlHref || '')

  const downloadUrl = redirectUrl.searchParams.get('url')

  return downloadUrl
}

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
