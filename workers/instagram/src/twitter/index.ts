import * as cheerio from 'cheerio'

export async function fetchTwitterVideoInfo(url: string) {
  const apiUrl = new URL('https://twitsave.com/info')
  apiUrl.searchParams.append('url', url)

  const html = await fetch(apiUrl).then((res) => res.text())

  const $ = cheerio.load(html)

  const link = $('.origin-top-right a').first()
  const videoUrl = link.attr('href')

  const description = $('.leading-tight p.m-2').first().text()

  const twitterVideoInfo = {
    contentUrl: videoUrl,
    source: 'twitter',
    sourceUrl: url,
    description,
    slug: stringToSlug(description),
  }
  return twitterVideoInfo
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
