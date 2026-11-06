import useDictStore from '@/store/modules/dict'
import { getDicts } from '@/api/system/dict/data'

/**
 * 获取字典数据
 */
export function useDict(...args) {
  const res = ref({})
  return (() => {
    args.forEach((dictType, index) => {
      res.value[dictType] = []
      const dicts = useDictStore().getDict(dictType)
      if (dicts) {
        // 历史缓存可能存在 number 类型 value（MySQL VARCHAR 被 Jackson 序列化成 number），统一强制转 string
        res.value[dictType] = dicts.map(d => ({ ...d, value: String(d.value) }))
      } else {
        getDicts(dictType).then(resp => {
          // 无论后端返回 number 还是 string，都强制转 string，保证 el-select :value 类型一致
          res.value[dictType] = resp.data.map(p => ({ label: p.dictLabel, value: String(p.dictValue), elTagType: p.listClass, elTagClass: p.cssClass }))
          useDictStore().setDict(dictType, res.value[dictType])
        })
      }
    })
    return toRefs(res.value)
  })()
}