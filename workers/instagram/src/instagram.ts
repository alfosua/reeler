export async function fetchVideoInfo(url: string) {
  const postId = getPostIdFromUrl(url)

  if (!postId) {
    throw new Error('Instagram post ID not found.')
  }

  const videoInfo = await getVideoInfo(postId)

  return videoInfo
}

const InstagramEndpoints = {
  GetByPost: `/p`,
  GetByGraphQL: `/api/graphql`,
} as const

class APIClient {
  baseURL: string

  constructor(baseURL: string) {
    this.baseURL = baseURL
  }

  _getRequestUrl(endpoint: string, options?: APIClientOptions) {
    if (options?.noBaseURL) {
      return endpoint
    }

    const baseUrl = options?.baseURL ?? this.baseURL

    if (!endpoint.startsWith('/')) {
      return `${baseUrl}/${endpoint}`
    }

    return `${baseUrl}${endpoint}`
  }

  async fetch(url: string, options?: APIClientOptions) {
    let response

    try {
      response = await fetch(url, options)

      if (!response.ok) {
        // const errorMessage = await this._getResponseErrors(response)
        // throw new Error(errorMessage)
        throw new Error('Oops! looks like something went wrong')
      }
    } catch (error: any) {
      // if (error.name === "AbortError") {
      //   throw new CustomError("The request was aborted by the user");
      // } else if (error.name === "SyntaxError" || error.name === "TypeError") {
      //   throw new CustomError("Oops! Looks like the client is having issues");
      // }

      // if (error.name === "NetworkError") {
      //   throw new NetworkError(
      //     "Network error, check your internet connection and try again"
      //   );
      // } else if (error.name === "SecurityError") {
      //   throw new NetworkError(
      //     "We are having issues connecting to the server. Please try again later"
      //   );
      // }

      // if (error instanceof CustomError) {
      //   throw error;
      // }

      throw new Error('Oops! looks like something went wrong')
    }

    return response
  }

  async post(endpoint: string, options?: APIClientOptions) {
    const requestUrl = this._getRequestUrl(endpoint, options)

    return this.fetch(requestUrl, {
      ...options,
      method: 'POST',
    })
  }
}

const apiClient = new APIClient('/api')

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
  const width = data.dimensions.width.toString()
  const height = data.dimensions.height.toString()
  const videoUrl = data.video_url

  const videoJson: VideoInfo = {
    filename,
    width,
    height,
    contentUrl: videoUrl,
    raw: data,
  }

  return videoJson
}

const getIGVideoFileName = () => getTimedFilename('ig-downloader', 'mp4')

const getTimedFilename = (name: string, ext: string) => {
  const timeStamp = Math.floor(Date.now() / 1000).toString()
  return `${name}-${timeStamp}.${ext}`
}

async function getPostGraphqlData({
  postId,
}: {
  postId: string
}): Promise<GraphQLResponse> {
  const encodedData = encodeGraphqlRequestData(postId)

  const res = await apiClient.post(InstagramEndpoints.GetByGraphQL, {
    baseURL: 'https://www.instagram.com',
    body: encodedData,
    headers: {
      Accept: '*/*',
      'Accept-Language': 'en-US,en;q=0.5',
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-FB-Friendly-Name': 'PolarisPostActionLoadPostQueryQuery',
      'X-CSRFToken': 'RVDUooU5MYsBbS1CNN3CzVAuEP8oHB52',
      'X-IG-App-ID': '1217981644879628',
      'X-FB-LSD': 'AVqbxe3J_YA',
      'X-ASBD-ID': '129477',
      'Sec-Fetch-Dest': 'empty',
      'Sec-Fetch-Mode': 'cors',
      'Sec-Fetch-Site': 'same-origin',
      'User-Agent':
        'Mozilla/5.0 (Linux; Android 11; SAMSUNG SM-G973U) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/14.2 Chrome/87.0.4280.141 Mobile Safari/537.36',
    },
  })

  const data: GraphQLResponse = await res.json()

  return data
}

function encodeGraphqlRequestData(shortcode: string) {
  const requestData = {
    av: '0',
    __d: 'www',
    __user: '0',
    __a: '1',
    __req: '3',
    __hs: '19624.HYP:instagram_web_pkg.2.1..0.0',
    dpr: '3',
    __ccg: 'UNKNOWN',
    __rev: '1008824440',
    __s: 'xf44ne:zhh75g:xr51e7',
    __hsi: '7282217488877343271',
    __dyn:
      '7xeUmwlEnwn8K2WnFw9-2i5U4e0yoW3q32360CEbo1nEhw2nVE4W0om78b87C0yE5ufz81s8hwGwQwoEcE7O2l0Fwqo31w9a9x-0z8-U2zxe2GewGwso88cobEaU2eUlwhEe87q7-0iK2S3qazo7u1xwIw8O321LwTwKG1pg661pwr86C1mwraCg',
    __csr:
      'gZ3yFmJkillQvV6ybimnG8AmhqujGbLADgjyEOWz49z9XDlAXBJpC7Wy-vQTSvUGWGh5u8KibG44dBiigrgjDxGjU0150Q0848azk48N09C02IR0go4SaR70r8owyg9pU0V23hwiA0LQczA48S0f-x-27o05NG0fkw',
    __comet_req: '7',
    lsd: 'AVqbxe3J_YA',
    jazoest: '2957',
    __spin_r: '1008824440',
    __spin_b: 'trunk',
    __spin_t: '1695523385',
    fb_api_caller_class: 'RelayModern',
    fb_api_req_friendly_name: 'PolarisPostActionLoadPostQueryQuery',
    variables: JSON.stringify({
      shortcode: shortcode,
      fetch_comment_count: 'null',
      fetch_related_profile_media_count: 'null',
      parent_comment_count: 'null',
      child_comment_count: 'null',
      fetch_like_count: 'null',
      fetch_tagged_user_count: 'null',
      fetch_preview_comment_count: 'null',
      has_threaded_comments: 'false',
      hoisted_comment_id: 'null',
      hoisted_reply_id: 'null',
    }),
    server_timestamps: 'true',
    doc_id: '10015901848480474',
  }
  const encoded = new URLSearchParams(requestData).toString()
  return encoded
}

type APIClientOptions = RequestInit & {
  baseURL?: string
  noBaseURL?: boolean
  authRetries?: number
  withCredentials?: boolean
}

type GraphQLResponse = {
  data: {
    xdt_shortcode_media: MediaData
  }
  extensions: {
    is_final: boolean
  }
}

type MediaData = {
  __typename: string
  __isXDTGraphMediaInterface: string
  id: string
  shortcode: string
  thumbnail_src: string
  dimensions: {
    height: number
    width: number
  }
  gating_info: any
  fact_check_overall_rating: any
  fact_check_information: any
  sensitivity_friction_info: any
  sharing_friction_info: {
    should_have_sharing_friction: boolean
    bloks_app_url: any
  }
  media_overlay_info: any
  media_preview: string
  display_url: string
  display_resources: Array<{
    src: string
    config_width: number
    config_height: number
  }>
  accessibility_caption: any
  dash_info: {
    is_dash_eligible: boolean
    video_dash_manifest: any
    number_of_qualities: number
  }
  has_audio: boolean
  video_url: string
  video_view_count: number
  video_play_count: number
  encoding_status: any
  is_published: boolean
  product_type: string
  title: string
  video_duration: number
  clips_music_attribution_info: {
    artist_name: string
    song_name: string
    uses_original_audio: boolean
    should_mute_audio: boolean
    should_mute_audio_reason: string
    audio_id: string
  }
  is_video: boolean
  tracking_token: string
  upcoming_event: any
  edge_media_to_tagged_user: {
    edges: Array<any>
  }
  owner: {
    id: string
    username: string
    is_verified: boolean
    profile_pic_url: string
    blocked_by_viewer: boolean
    restricted_by_viewer: any
    followed_by_viewer: boolean
    full_name: string
    has_blocked_viewer: boolean
    is_embeds_disabled: boolean
    is_private: boolean
    is_unpublished: boolean
    requested_by_viewer: boolean
    pass_tiering_recommendation: boolean
    edge_owner_to_timeline_media: {
      count: number
    }
    edge_followed_by: {
      count: number
    }
  }
  edge_media_to_caption: {
    edges: Array<{
      node: {
        created_at: string
        text: string
        id: string
      }
    }>
  }
  can_see_insights_as_brand: boolean
  caption_is_edited: boolean
  has_ranked_comments: boolean
  like_and_view_counts_disabled: boolean
  edge_media_to_comment: {
    count: number
    page_info: {
      has_next_page: boolean
      end_cursor: string
    }
    edges: Array<any>
  }
  comments_disabled: boolean
  commenting_disabled_for_viewer: boolean
  taken_at_timestamp: number
  edge_media_preview_like: {
    count: number
    edges: Array<any>
  }
  edge_media_to_sponsor_user: {
    edges: Array<any>
  }
  is_affiliate: boolean
  is_paid_partnership: boolean
  location: any
  nft_asset_info: any
  viewer_has_liked: boolean
  viewer_has_saved: boolean
  viewer_has_saved_to_collection: boolean
  viewer_in_photo_of_you: boolean
  viewer_can_reshare: boolean
  is_ad: boolean
  edge_web_media_to_related_media: {
    edges: Array<any>
  }
  coauthor_producers: Array<any>
  pinned_for_users: Array<any>
  edge_related_profiles: {
    edges: Array<{
      node: {
        id: string
        full_name: string
        is_private: boolean
        is_verified: boolean
        profile_pic_url: string
        username: string
        edge_followed_by: {
          count: number
        }
        edge_owner_to_timeline_media: {
          count: number
          edges: Array<any>
        }
      }
    }>
  }
}
