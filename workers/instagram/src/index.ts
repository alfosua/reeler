import { Hono } from 'hono'
import { HTTPException } from 'hono/http-exception'
import { fetchVideoInfo } from './instagram'
import { stream } from 'hono/streaming'

const app = new Hono()

app.get('/video-info', async (c) => {
  try {
    const requestUrl = new URL(c.req.url)
    const { url } = c.req.query()
    if (!url) {
      throw new Error('Target url cannot be null')
    }

    const result = await fetchVideoInfo(url)
    return c.json({
      ...result,
      contentUrl: `${requestUrl.origin}/video-download/${btoa(
        result.contentUrl,
      )}.mp4`,
    })
  } catch (e) {
    handleErrorAsHTTPException(e)
  }
})

app.get('/video-download/:key', async (c) => {
  try {
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

    c.header(
      'Content-Type',
      response.headers.get('content-type') || 'video/mp4',
    )
    c.header(
      'Content-Disposition',
      'attachment; filename="downloaded-video.mp4"',
    )

    return stream(c, async (stream) => {
      if (response.body !== null) {
        await stream.pipe(response.body)
      }
    })
  } catch (e) {
    handleErrorAsHTTPException(e)
  }
})

function handleErrorAsHTTPException(e: unknown) {
  if (typeof e === 'string') {
    throw new HTTPException(500, { message: e })
  } else if (e instanceof Error) {
    throw new HTTPException(500, { message: e.message })
  }
  throw new HTTPException(500, { message: 'Unknown error' })
}

export default app
