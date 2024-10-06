import { fetchInstagramVideoInfo } from './instagram'
import { fetchTiktokVideoInfo } from './tiktok'
import { fetchTwitterVideoInfo } from './twitter'

export function getFetcherByHostname(hostname: string) {
  switch (hostname) {
    case 'www.tiktok.com':
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
