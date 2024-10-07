import { fetchInstagramVideoInfo } from './instagram'
import { fetchTiktokVideoInfo } from './tiktok'
import { fetchTwitterVideoInfo } from './twitter'
import { fetchYoutubeVideoInfo } from './youtube'

export function getFetcherByHostname(hostname: string) {
  switch (hostname) {
    case 'www.youtube.com':
    case 'm.youtube.com':
    case 'youtube.com':
    case 'youtu.be': {
      return fetchYoutubeVideoInfo
    }

    case 'www.tiktok.com':
    case 'vm.tiktok.com':
    case 'tiktok.com': {
      return fetchTiktokVideoInfo
    }

    case 'www.instagram.com':
    case 'instagram.com': {
      return fetchInstagramVideoInfo
    }

    case 'x.com':
    case 'twitter.com': {
      return fetchTwitterVideoInfo
    }

    default:
      throw new Error('Not supported')
  }
}
