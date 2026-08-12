package com.ruoyi.qms.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * AQL抽样计算工具（GB/T 2828.1）
 * 内置批量→样本量字码→n/Ac/Re的简化抽样表
 *
 * @author ruoyi
 */
public class AqlCalculator
{
    /** 样本量字码对应样本量 */
    private static final Map<String, Integer> SAMPLE_SIZE_MAP = new HashMap<>();
    /** AQL → (字码 → Ac/Re) 二维表 */
    private static final Map<String, Map<String, int[]>> AQL_TABLE = new HashMap<>();

    static
    {
        // 样本量字码 → 样本量（GB/T 2828.1 正常检验）
        SAMPLE_SIZE_MAP.put("A", 2);
        SAMPLE_SIZE_MAP.put("B", 3);
        SAMPLE_SIZE_MAP.put("C", 5);
        SAMPLE_SIZE_MAP.put("D", 8);
        SAMPLE_SIZE_MAP.put("E", 13);
        SAMPLE_SIZE_MAP.put("F", 20);
        SAMPLE_SIZE_MAP.put("G", 32);
        SAMPLE_SIZE_MAP.put("H", 50);
        SAMPLE_SIZE_MAP.put("J", 80);
        SAMPLE_SIZE_MAP.put("K", 125);
        SAMPLE_SIZE_MAP.put("L", 200);
        SAMPLE_SIZE_MAP.put("M", 315);
        SAMPLE_SIZE_MAP.put("N", 500);
        SAMPLE_SIZE_MAP.put("P", 800);
        SAMPLE_SIZE_MAP.put("Q", 1250);
        SAMPLE_SIZE_MAP.put("R", 2000);

        // AQL 0.65
        Map<String, int[]> aql065 = new HashMap<>();
        aql065.put("G", new int[]{0, 1});
        aql065.put("H", new int[]{1, 2});
        aql065.put("J", new int[]{1, 2});
        aql065.put("K", new int[]{2, 3});
        aql065.put("L", new int[]{3, 4});
        aql065.put("M", new int[]{5, 6});
        aql065.put("N", new int[]{7, 8});
        aql065.put("P", new int[]{10, 11});
        aql065.put("Q", new int[]{14, 15});
        aql065.put("R", new int[]{21, 22});
        AQL_TABLE.put("0.65", aql065);

        // AQL 1.0
        Map<String, int[]> aql10 = new HashMap<>();
        aql10.put("G", new int[]{1, 2});
        aql10.put("H", new int[]{1, 2});
        aql10.put("J", new int[]{2, 3});
        aql10.put("K", new int[]{3, 4});
        aql10.put("L", new int[]{5, 6});
        aql10.put("M", new int[]{7, 8});
        aql10.put("N", new int[]{10, 11});
        aql10.put("P", new int[]{14, 15});
        aql10.put("Q", new int[]{21, 22});
        AQL_TABLE.put("1.0", aql10);

        // AQL 1.5
        Map<String, int[]> aql15 = new HashMap<>();
        aql15.put("F", new int[]{0, 1});
        aql15.put("G", new int[]{1, 2});
        aql15.put("H", new int[]{2, 3});
        aql15.put("J", new int[]{3, 4});
        aql15.put("K", new int[]{5, 6});
        aql15.put("L", new int[]{7, 8});
        aql15.put("M", new int[]{10, 11});
        aql15.put("N", new int[]{14, 15});
        aql15.put("P", new int[]{21, 22});
        AQL_TABLE.put("1.5", aql15);

        // AQL 2.5
        Map<String, int[]> aql25 = new HashMap<>();
        aql25.put("E", new int[]{0, 1});
        aql25.put("F", new int[]{1, 2});
        aql25.put("G", new int[]{2, 3});
        aql25.put("H", new int[]{3, 4});
        aql25.put("J", new int[]{5, 6});
        aql25.put("K", new int[]{7, 8});
        aql25.put("L", new int[]{10, 11});
        aql25.put("M", new int[]{14, 15});
        aql25.put("N", new int[]{21, 22});
        AQL_TABLE.put("2.5", aql25);

        // AQL 4.0
        Map<String, int[]> aql40 = new HashMap<>();
        aql40.put("E", new int[]{0, 1});
        aql40.put("F", new int[]{1, 2});
        aql40.put("G", new int[]{2, 3});
        aql40.put("H", new int[]{5, 6});
        aql40.put("J", new int[]{7, 8});
        aql40.put("K", new int[]{10, 11});
        aql40.put("L", new int[]{14, 15});
        aql40.put("M", new int[]{21, 22});
        AQL_TABLE.put("4.0", aql40);
    }

    /**
     * 根据批量确定样本量字码（正常检验）
     */
    public static String getCodeLetter(BigDecimal batchQty)
    {
        int qty = batchQty.intValue();
        if (qty <= 2) return "A";
        if (qty <= 8) return "B";
        if (qty <= 15) return "C";
        if (qty <= 25) return "D";
        if (qty <= 50) return "E";
        if (qty <= 90) return "F";
        if (qty <= 150) return "G";
        if (qty <= 280) return "H";
        if (qty <= 500) return "J";
        if (qty <= 1200) return "K";
        if (qty <= 3200) return "L";
        if (qty <= 10000) return "M";
        if (qty <= 35000) return "N";
        if (qty <= 150000) return "P";
        if (qty <= 500000) return "Q";
        return "R";
    }

    /**
     * 计算抽样结果
     * @param batchQty 批量
     * @param aqlLevel AQL等级（如 "1.0", "2.5"）
     * @return int[]{样本量n, 接收数Ac, 拒收数Re}，null表示需人工指定
     */
    public static int[] calculate(BigDecimal batchQty, String aqlLevel)
    {
        String codeLetter = getCodeLetter(batchQty);
        Integer n = SAMPLE_SIZE_MAP.get(codeLetter);
        if (n == null)
        {
            return null;
        }
        Map<String, int[]> levelTable = AQL_TABLE.get(aqlLevel);
        if (levelTable == null)
        {
            // 默认使用 AQL 1.0
            levelTable = AQL_TABLE.get("1.0");
        }
        int[] acRe = levelTable.get(codeLetter);
        if (acRe == null)
        {
            // 该字码下无对应Ac/Re，向下查找到最近可用字码
            for (Map.Entry<String, int[]> entry : levelTable.entrySet())
            {
                if (SAMPLE_SIZE_MAP.getOrDefault(entry.getKey(), 0) >= n)
                {
                    acRe = entry.getValue();
                    n = SAMPLE_SIZE_MAP.get(entry.getKey());
                    break;
                }
            }
        }
        if (acRe == null)
        {
            return new int[]{n, 0, 1};
        }
        return new int[]{n, acRe[0], acRe[1]};
    }

    /**
     * 批量判定
     * @param defectCount 不合格数
     * @param ac 接收数
     * @param re 拒收数
     * @return true=合格（接收），false=不合格（拒收）
     */
    public static boolean judgeBatch(int defectCount, int ac, int re)
    {
        return defectCount <= ac;
    }
}
