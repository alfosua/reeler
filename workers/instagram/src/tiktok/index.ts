import * as cheerio from 'cheerio'
import { getFilenameByTimestamp } from '../utils'

export async function fetchTiktokVideoInfo(url: string) {
  const contentUrl = await getContentUrl(url)

  const result: VideoInfo = {
    contentUrl: contentUrl || '',
    filename: getFilenameByTimestamp('mp4'),
    source: 'tiktok',
    sourceUrl: url,
  }

  return result
}

export async function getContentUrl(url: string) {
  const response = await fetch('https://ttdownloader.com/')
  const html = await response.text()
  const $ = cheerio.load(html)
  const cookie = response.headers.get('set-cookie')
  const token = $('#token').attr('value')

  const data = new URLSearchParams()
  data.append('url', url)
  data.append('format', '')
  data.append('token', token || '')

  const searchResponse = await fetch('https://ttdownloader.com/search/', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
      Origin: 'https://ttdownloader.com',
      Referer: 'https://ttdownloader.com/',
      Cookie: cookie || '',
    },
    body: data.toString(),
  })

  const searchHtml = await searchResponse.text()
  const $search = cheerio.load(searchHtml)

  const withNoWatermark = $search(
    '#results-list > div:nth-child(2) > div.download > a',
  )?.attr('href')
  const withWatermark = $search(
    '#results-list > div:nth-child(3) > div.download > a',
  )?.attr('href')

  return withNoWatermark || withWatermark
}
