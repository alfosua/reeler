import { getPostGraphqlData, MediaData } from './api'

export async function fetchVideoInfo(url: string) {
  const postId = getPostIdFromUrl(url)

  if (!postId) {
    throw new Error('Instagram post ID not found.')
  }

  const videoInfo = await getVideoInfo(postId)

  return videoInfo
}

function getPostIdFromUrl(postUrl: string) {
  const postRegex =
    /^https:\/\/(?:www\.)?instagram\.com\/p\/([a-zA-Z0-9_-]+)\/?/
  const reelRegex =
    /^https:\/\/(?:www\.)?instagram\.com\/reels?\/([a-zA-Z0-9_-]+)\/?/

  return postUrl.match(postRegex)?.at(-1) || postUrl.match(reelRegex)?.at(-1)
}

async function getVideoInfo(postId: string) {
  let videoInfo: VideoInfo | null = null

  videoInfo = await getVideoJSONFromGraphQL(postId)
  if (videoInfo) return videoInfo

  throw new Error('Video link for this post is not public.')
}

async function getVideoJSONFromGraphQL(postId: string) {
  const data = await getPostGraphqlData({ postId })

  const mediaData = data.data?.xdt_shortcode_media

  if (!mediaData) {
    return null
  }

  if (!mediaData.is_video) {
    throw new Error('This post is not a video')
  }

  const videoInfo = formatGraphqlJson(mediaData)
  return videoInfo
}

const formatGraphqlJson = (data: MediaData) => {
  const filename = getIGVideoFileName()
  const width = data.dimensions.width
  const height = data.dimensions.height
  const username = data.owner.username
  const caption = data.edge_media_to_caption.edges[0].node?.text.split('\n')[0]
  const duration = data.video_duration
  const userAvatarUrl = data.owner.profile_pic_url
  const thumbnailUrl = data.thumbnail_src
  const contentUrl = data.video_url

  const videoJson: VideoInfo = {
    filename,
    contentUrl,
    width,
    height,
    username,
    caption,
    duration,
    userAvatarUrl,
    thumbnailUrl,
    raw: data,
  }

  return videoJson
}

const getIGVideoFileName = () => getTimedFilename('ig-downloader', 'mp4')

const getTimedFilename = (name: string, ext: string) => {
  const timeStamp = Math.floor(Date.now() / 1000)
  return `${name}-${timeStamp}.${ext}`
}
