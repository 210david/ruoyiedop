import request from '@/utils/request'

export function listOee(query) {
  return request({ url: '/mms/oee/list', method: 'get', params: query })
}
export function getOee(snapshotId) {
  return request({ url: '/mms/oee/' + snapshotId, method: 'get' })
}
