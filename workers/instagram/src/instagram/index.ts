import { getFilenameByTimestamp } from '../utils'
import { getPostGraphqlData, MediaData } from './api'

export async function fetchInstagramVideoInfo(url: string) {
  const postId = getPostIdFromUrl(url)

  if (!postId) {
    throw new Error('Instagram post ID not found.')
  }

  const metadata = await getVideoJSONFromGraphQL(postId)
  const result: VideoInfo = {
    filename: getFilenameByTimestamp('mp4'),
    source: 'instagram',
    sourceUrl: url,
    ...metadata,
  }

  return result
}

function getPostIdFromUrl(postUrl: string) {
  const postRegex =
    /^https:\/\/(?:www\.)?instagram\.com\/p\/([a-zA-Z0-9_-]+)\/?/
  const reelRegex =
    /^https:\/\/(?:www\.)?instagram\.com\/reels?\/([a-zA-Z0-9_-]+)\/?/

  return postUrl.match(postRegex)?.at(-1) || postUrl.match(reelRegex)?.at(-1)
}

async function getVideoJSONFromGraphQL(postId: string) {
  const data = await getPostGraphqlData({ postId })

  const mediaData = data.data?.xdt_shortcode_media

  if (!mediaData) {
    throw new Error('Video link for this post is not public.')
  }

  if (!mediaData.is_video) {
    throw new Error('This post is not a video')
  }

  const videoInfo = formatGraphqlJson(mediaData)
  return videoInfo
}

function formatGraphqlJson(data: MediaData) {
  return {
    width: data.dimensions.width,
    height: data.dimensions.height,
    username: data.owner.username,
    caption: data.edge_media_to_caption.edges[0].node?.text.split('\n')[0],
    duration: data.video_duration,
    userAvatarUrl: data.owner.profile_pic_url,
    thumbnailUrl: data.thumbnail_src,
    contentUrl: data.video_url,
    raw: data,
  }
}
