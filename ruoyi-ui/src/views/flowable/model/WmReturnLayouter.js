/**
 * 泛微式退回线布局器：退回/驳回出口自动正交绕行
 *
 * 路由口径（参照泛微）：从源节点上方出发 → 上升到绕行高度 → 横向跨越 → 下降进入目标节点顶部，
 * 其余连线（前进线）委托默认布局（Manhattan）。
 *
 * 判定：bpmn:SequenceFlow 且 flowable:exitMeta.isReturn 为真，或出口名称含「退回/驳回」。
 */
import BaseLayouter from 'diagram-js/lib/layout/BaseLayouter'
import { getMid } from 'diagram-js/lib/layout/LayoutUtil'

const BYPASS_GAP = 60

export default class WmReturnLayouter extends BaseLayouter {
  constructor(connectionDocking) {
    super()
    this._connectionDocking = connectionDocking
  }
}

WmReturnLayouter.$inject = ['connectionDocking']

WmReturnLayouter.prototype.layoutConnection = function (connection, hints) {
  if (isReturnExit(connection) && connection.source && connection.target) {
    return this.routeReturn(connection)
  }
  return BaseLayouter.prototype.layoutConnection.call(this, connection, hints)
}

/** 退回线路由：源上边中点 → 绕行高度 → 目标上边中点（dock 裁剪起止） */
WmReturnLayouter.prototype.routeReturn = function (connection) {
  const source = connection.source
  const target = connection.target
  const sMid = getMid(source)
  const tMid = getMid(target)
  const bypassY = Math.min(source.y, target.y) - BYPASS_GAP
  let waypoints = [
    { x: sMid.x, y: source.y },
    { x: sMid.x, y: bypassY },
    { x: tMid.x, y: bypassY },
    { x: tMid.x, y: target.y }
  ]
  try {
    waypoints = this._connectionDocking.getCroppedWaypoints({ waypoints }, source, target)
  } catch (e) {
    // 裁剪失败保留原始绕行路径
  }
  return waypoints
}

/** 退回出口判定（供布局器与画布重布局共用） */
export function isReturnExit(connection) {
  if (!connection || connection.type !== 'bpmn:SequenceFlow') return false
  const bo = connection.businessObject
  if (!bo) return false
  let meta = {}
  try {
    meta = JSON.parse((bo.get && bo.get('flowable:exitMeta')) || (bo.$attrs && bo.$attrs['flowable:exitMeta']) || '{}') || {}
  } catch (e) {
    meta = {}
  }
  return !!meta.isReturn || /退回|驳回/.test(bo.name || '')
}
