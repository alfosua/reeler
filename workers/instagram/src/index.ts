import { Hono } from 'hono'
import { HTTPException } from 'hono/http-exception'
import { getFetcherByHostname } from './fetchers'

const app = new Hono()

app.get('/video-info', async (c) => {
  let url
  try {
    url = new URL(c.req.query('url')!)
  } catch (e) {
    console.error(String(e), c.req.query('url'))
    c.status(400)
    return c.json({ error: true, message: 'Invalid URL', code: 'INVALID_URL' })
  }

  const href = url.href
  const fetchVideoInfo = getFetcherByHostname(url.hostname)
  const videoInfo = await fetchVideoInfo(href)

  return c.json(videoInfo)
})

app.get('/video-download/:key', async (c) => {
  const { key } = c.req.param()
  const base64 = key.replace(/(\.mp4)$/g, '')
  console.log(base64)
  const targetUrl = atob(base64)

  if (!targetUrl) {
    throw new Error('Please provide a valid video URL')
  }

  const response = await fetch(targetUrl)

  if (!response.ok) {
    throw new Error(`Failed to download video: ${response.status}`)
  }

  c.header('Content-Type', response.headers.get('content-type') || 'video/mp4')
  c.header('Content-Disposition', 'attachment; filename="downloaded-video.mp4"')

  return c.body(response.body as ReadableStream)
})

app.onError((err, c) => {
  if (err instanceof HTTPException) {
    console.error(String(err.cause || err))
    return err.getResponse()
  }
  console.error(`Error: ${err}`)

  let message: string

  if (typeof err === 'string') {
    message = err
  } else if (err instanceof Error) {
    message = String(err)
  } else {
    message = 'Unknown error'
  }

  c.status(500)
  return c.json({ error: true, message: err.message })
})

export default app
