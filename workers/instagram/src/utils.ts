export function getFilenameByTimestamp(ext: string) {
  const timeStamp = Math.floor(Date.now() / 1000)
  return `reeler-${timeStamp}.${ext}`
}
