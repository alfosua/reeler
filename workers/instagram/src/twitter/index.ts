import * as cheerio from 'cheerio'
import { getFilenameByTimestamp } from '../utils'

export async function fetchTwitterVideoInfo(url: string) {
  const apiUrl = new URL('https://twitsave.com/info')
  apiUrl.searchParams.append('url', url)

  const html = await fetch(apiUrl).then((res) => res.text())

  const $ = cheerio.load(html)

  const link = $('.origin-top-right a').first()
  const videoUrl = link.attr('href')

  const description = $('.leading-tight p.m-2').first().text()

  const result: VideoInfo = {
    filename: getFilenameByTimestamp('mp4'),
    contentUrl: videoUrl || '',
    source: 'twitter',
    sourceUrl: url,
    caption: description,
  }

  return result
}

function stringToSlug(s: string) {
  return s
    .toLowerCase()
    .normalize('NFKD')
    .toLowerCase()
    .replace(/[^\w\s-]/g, '')
    .trim()
    .replace(/[-\s]+/g, '-')
}
