from fastapi import FastAPI, HTTPException
from fastapi.responses import StreamingResponse
import httpx
import yt_dlp

app = FastAPI()

@app.get("/")
async def root():
    return {"message": "Hello World"}

@app.get("/video/download")
async def video_download_endpoint(url: str):
    ydl_opts = {
        'format': 'bestvideo+bestaudio/best',
        'noplaylist': True,
        'quiet': True,
    }

    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info_dict = ydl.extract_info(url, download=False)

            if 'formats' not in info_dict:
                raise HTTPException(status_code=400, detail="No downloadable formats found")
            
            best_format = info_dict['formats'][-1]
            stream_url = best_format['url']
            mime_type = best_format.get('mime_type') or 'application/octet-stream'
            file_ext = best_format.get('ext') or 'unknown'
            title = info_dict.get('title', 'video')

            async def generate():
                async with httpx.AsyncClient() as client:
                    async with client.stream('GET', stream_url) as response:
                        if response.status_code != 200:
                            raise HTTPException(status_code=500, detail=f'Error when streaming video: {response.status_code}')
                        async for chunk in response.aiter_bytes(chunk_size=8192):
                            yield chunk
                
            headers = {
                'Content-Type': mime_type,
                'Content-Disposition': f'attachment; filename="{title}.{file_ext}"'
            }
            return StreamingResponse(generate(), media_type=mime_type, headers=headers)
    except HTTPException as http_exc:
        raise http_exc
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
