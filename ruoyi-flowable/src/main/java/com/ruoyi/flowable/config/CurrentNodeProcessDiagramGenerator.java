package com.ruoyi.flowable.config;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.image.impl.DefaultProcessDiagramCanvas;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.stereotype.Component;

/**
 * 自定义流程图生成器：在默认高亮（绿色=已流转）基础上，
 * 对「当前停留节点」额外绘制红色粗边框，便于一眼定位流程当前位置。
 *
 * 用法：generateDiagram(bpmnModel, imageType, highLightedActivities, highLightedFlows,
 *                       currentActivityIds, ...) —— currentActivityIds 中的节点红框标注。
 */
@Component("currentNodeDiagramGenerator")
public class CurrentNodeProcessDiagramGenerator extends DefaultProcessDiagramGenerator
{
    /** 当前停留节点标注色（红），与前端图例 #ef4444 一致 */
    private static final Color CURRENT_NODE_COLOR = new Color(0xEF, 0x44, 0x44);

    /** 结束事件标注色（红实心圆心） */
    private static final Color END_EVENT_COLOR = new Color(0xDC, 0x26, 0x26);

    private static final Stroke CURRENT_NODE_STROKE = new BasicStroke(3.0f);

    /** 当前停留节点（ThreadLocal 传递，避免污染父类公开签名） */
    private static final ThreadLocal<List<String>> CURRENT_NODES = new ThreadLocal<>();

    public InputStream generateDiagramWithCurrent(BpmnModel bpmnModel, String imageType,
            List<String> highLightedActivities, List<String> highLightedFlows,
            List<String> currentActivityIds, String activityFontName, String labelFontName,
            String annotationFontName, ClassLoader customClassLoader, double scaleFactor, boolean drawSequenceFlowNameWithNoLabel)
    {
        CURRENT_NODES.set(currentActivityIds);
        try
        {
            return generateDiagram(bpmnModel, imageType, highLightedActivities, highLightedFlows,
                    activityFontName, labelFontName, annotationFontName, customClassLoader, scaleFactor,
                    drawSequenceFlowNameWithNoLabel);
        }
        finally
        {
            CURRENT_NODES.remove();
        }
    }

    /**
     * 重写 drawActivity：在父类完成默认绘制（含绿色高亮）后，
     * 若该节点属于当前停留节点，则再叠加红色粗边框。
     */
    @Override
    protected void drawActivity(DefaultProcessDiagramCanvas canvas, BpmnModel bpmnModel,
            org.flowable.bpmn.model.FlowNode flowNode, List<String> highLightedActivities,
            List<String> highLightedFlows, double scaleFactor, Boolean isExpanded)
    {
        super.drawActivity(canvas, bpmnModel, flowNode, highLightedActivities, highLightedFlows,
                scaleFactor, isExpanded);
        List<String> currents = CURRENT_NODES.get();
        if (currents != null && !currents.isEmpty() && currents.contains(flowNode.getId()))
        {
            GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
            if (graphicInfo != null)
            {
                drawCurrentNodeHighLight(canvas, graphicInfo);
            }
        }
        // 结束事件：叠加红色实心圆心，与开始事件（细边空心圆）明确区分
        if (flowNode instanceof org.flowable.bpmn.model.EndEvent)
        {
            GraphicInfo gi = bpmnModel.getGraphicInfo(flowNode.getId());
            if (gi != null)
            {
                drawEndEventMarker(canvas, gi);
            }
        }
    }

    /** 结束事件红色实心圆心标注（BPMN 惯例：结束=粗边+实心标记） */
    private void drawEndEventMarker(DefaultProcessDiagramCanvas canvas, GraphicInfo graphicInfo)
    {
        try
        {
            Field field = DefaultProcessDiagramCanvas.class.getDeclaredField("g");
            field.setAccessible(true);
            Graphics2D g = (Graphics2D) field.get(canvas);
            Paint oldPaint = g.getPaint();
            g.setPaint(END_EVENT_COLOR);
            double cx = graphicInfo.getX() + graphicInfo.getWidth() / 2.0;
            double cy = graphicInfo.getY() + graphicInfo.getHeight() / 2.0;
            double r = Math.min(graphicInfo.getWidth(), graphicInfo.getHeight()) * 0.18;
            g.fill(new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
            g.setPaint(oldPaint);
        }
        catch (Exception ignored)
        {
            // 静默降级
        }
    }

    /** 借助反射获取 canvas 的 Graphics2D（protected 字段 g），绘制红色粗边框圆角矩形 */
    private void drawCurrentNodeHighLight(DefaultProcessDiagramCanvas canvas, GraphicInfo graphicInfo)
    {
        try
        {
            Field field = DefaultProcessDiagramCanvas.class.getDeclaredField("g");
            field.setAccessible(true);
            Graphics2D g = (Graphics2D) field.get(canvas);
            Paint oldPaint = g.getPaint();
            Stroke oldStroke = g.getStroke();
            g.setPaint(CURRENT_NODE_COLOR);
            g.setStroke(CURRENT_NODE_STROKE);
            g.draw(new RoundRectangle2D.Double(graphicInfo.getX(), graphicInfo.getY(),
                    graphicInfo.getWidth(), graphicInfo.getHeight(), 6, 6));
            g.setPaint(oldPaint);
            g.setStroke(oldStroke);
        }
        catch (Exception ignored)
        {
            // 反射失败时静默降级：仅缺少红框标注，不影响主流程图
        }
    }
}
