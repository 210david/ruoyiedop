package com.ruoyi.qms.util;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * AQL抽样计算工具（GB/T 2828.1-2012）
 * 内置批量→样本量字码→n/Ac/Re的简化抽样表
 * <p>
 * 支持的检验水平：
 * <ul>
 *   <li>特殊检验水平：S-1, S-2, S-3, S-4</li>
 *   <li>一般检验水平：I, II, III</li>
 * </ul>
 * 支持的AQL等级：0.65, 1.0, 1.5, 2.5, 4.0
 * <p>
 * 检验严格度（正常/加严/放宽）暂以正常检验为主表实现，
 * 加严/放宽可通过切换 Ac/Re 表扩展。
 *
 * @author ruoyi
 */
public class AqlCalculator
{
    /** 样本量字码对应样本量（GB/T 2828.1 表2 — 样本量字码与样本量） */
    private static final Map<String, Integer> SAMPLE_SIZE_MAP = new LinkedHashMap<>();
    /** AQL → (字码 → Ac/Re) 二维表，内层使用 TreeMap 保证字码按字母序遍历（即样本量升序） */
    private static final Map<String, Map<String, int[]>> AQL_TABLE = new LinkedHashMap<>();

    /**
     * 批量区间 → 字码 的映射表，按检验水平组织。
     * 每个检验水平维护一组批量区间→字码的映射，对应 GB/T 2828.1 表1。
     * 使用 TreeMap（自然排序），key 为批量下限，value 为字码。
     */
    private static final Map<String, TreeMap<Integer, String>> CODE_LETTER_MAP = new LinkedHashMap<>();

    /** 默认检验水平 */
    private static final String DEFAULT_INSPECT_LEVEL = "II";

    static
    {
        // ============================================================
        // 1. 样本量字码 → 样本量（GB/T 2828.1 表2）
        // ============================================================
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

        // ============================================================
        // 2. 批量区间 → 字码（GB/T 2828.1 表1 — 样本量字码）
        //    TreeMap key = 批量下限, value = 字码
        // ============================================================

        // --- 特殊检验水平 S-1 ---
        TreeMap<Integer, String> s1 = new TreeMap<>();
        s1.put(1, "A");
        CODE_LETTER_MAP.put("S-1", s1);

        // --- 特殊检验水平 S-2 ---
        TreeMap<Integer, String> s2 = new TreeMap<>();
        s2.put(1, "A");     // 1 ~ 8
        s2.put(9, "B");     // 9 ~ 50（S-2: 9-50→B）
        CODE_LETTER_MAP.put("S-2", s2);

        // --- 特殊检验水平 S-3 ---
        TreeMap<Integer, String> s3 = new TreeMap<>();
        s3.put(1, "A");     // 1 ~ 8
        s3.put(9, "B");     // 9 ~ 35
        s3.put(36, "C");    // 36 ~ 150
        s3.put(151, "D");   // 151 ~ 500
        s3.put(501, "E");   // 501+
        CODE_LETTER_MAP.put("S-3", s3);

        // --- 特殊检验水平 S-4 ---
        TreeMap<Integer, String> s4 = new TreeMap<>();
        s4.put(1, "A");     // 1 ~ 8
        s4.put(9, "B");     // 9 ~ 15
        s4.put(16, "C");    // 16 ~ 25
        s4.put(26, "D");    // 26 ~ 50
        s4.put(51, "E");    // 51 ~ 90
        s4.put(91, "F");    // 91 ~ 150
        s4.put(151, "G");   // 151 ~ 280
        s4.put(281, "H");   // 281 ~ 400
        s4.put(401, "J");   // 401 ~ 500
        s4.put(501, "K");   // 501 ~ 1200
        s4.put(1201, "L");  // 1201+
        CODE_LETTER_MAP.put("S-4", s4);

        // --- 一般检验水平 I ---
        TreeMap<Integer, String> level1 = new TreeMap<>();
        level1.put(1, "A");      // 1 ~ 15 (Level I: 1-8→A, 9-15→A)
        level1.put(16, "B");     // 16 ~ 25
        level1.put(26, "C");     // 26 ~ 50
        level1.put(51, "C");     // 51 ~ 90 (Level I: 51-90→C)
        level1.put(91, "D");     // 91 ~ 150
        level1.put(151, "D");    // 151 ~ 280 (Level I: 151-280→D)
        level1.put(281, "E");    // 281 ~ 500
        level1.put(501, "F");     // 501 ~ 1200
        level1.put(1201, "G");    // 1201 ~ 3200
        level1.put(3201, "H");    // 3201 ~ 10000
        level1.put(10001, "J");   // 10001 ~ 35000
        level1.put(35001, "K");   // 35001 ~ 150000
        level1.put(150001, "L");  // 150001 ~ 500000
        level1.put(500001, "M");  // 500001+
        CODE_LETTER_MAP.put("I", level1);

        // --- 一般检验水平 II（默认） ---
        TreeMap<Integer, String> level2 = new TreeMap<>();
        level2.put(1, "A");       // 1 ~ 8
        level2.put(9, "B");       // 9 ~ 15
        level2.put(16, "C");      // 16 ~ 25
        level2.put(26, "D");      // 26 ~ 50
        level2.put(51, "E");      // 51 ~ 90
        level2.put(91, "F");      // 91 ~ 150
        level2.put(151, "G");     // 151 ~ 280
        level2.put(281, "H");     // 281 ~ 500
        level2.put(501, "J");     // 501 ~ 1200
        level2.put(1201, "K");    // 1201 ~ 3200
        level2.put(3201, "L");    // 3201 ~ 10000
        level2.put(10001, "M");   // 10001 ~ 35000
        level2.put(35001, "N");   // 35001 ~ 150000
        level2.put(150001, "P");  // 150001 ~ 500000
        level2.put(500001, "Q");  // 500001+
        CODE_LETTER_MAP.put("II", level2);

        // --- 一般检验水平 III ---
        TreeMap<Integer, String> level3 = new TreeMap<>();
        level3.put(1, "C");       // 1 ~ 8
        level3.put(9, "D");       // 9 ~ 15
        level3.put(16, "E");      // 16 ~ 25
        level3.put(26, "F");      // 26 ~ 50
        level3.put(51, "G");      // 51 ~ 90
        level3.put(91, "H");      // 91 ~ 150
        level3.put(151, "J");     // 151 ~ 280
        level3.put(281, "K");     // 281 ~ 500
        level3.put(501, "L");     // 501 ~ 1200
        level3.put(1201, "M");    // 1201 ~ 3200
        level3.put(3201, "N");    // 3201 ~ 10000
        level3.put(10001, "P");   // 10001 ~ 35000
        level3.put(35001, "Q");   // 35001 ~ 150000
        level3.put(150001, "Q");  // 150001 ~ 500000
        level3.put(500001, "R");  // 500001+
        CODE_LETTER_MAP.put("III", level3);

        // ============================================================
        // 3. AQL → (字码 → Ac/Re) 正常检验一次抽样方案（GB/T 2828.1 表3-A）
        // ============================================================

        // AQL 0.65
        Map<String, int[]> aql065 = new TreeMap<>();
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
        Map<String, int[]> aql10 = new TreeMap<>();
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
        Map<String, int[]> aql15 = new TreeMap<>();
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
        Map<String, int[]> aql25 = new TreeMap<>();
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
        Map<String, int[]> aql40 = new TreeMap<>();
        aql40.put("D", new int[]{0, 1});
        aql40.put("E", new int[]{1, 2});
        aql40.put("F", new int[]{2, 3});
        aql40.put("G", new int[]{3, 4});
        aql40.put("H", new int[]{5, 6});
        aql40.put("J", new int[]{7, 8});
        aql40.put("K", new int[]{10, 11});
        aql40.put("L", new int[]{14, 15});
        aql40.put("M", new int[]{21, 22});
        AQL_TABLE.put("4.0", aql40);
    }

    /**
     * 根据批量确定样本量字码（GB/T 2828.1 表1 — 一般检验水平 II，默认）
     * <p>
     * 注意：此方法仅支持一般检验水平II。如需指定其他检验水平，请使用
     * {@link #getCodeLetter(BigDecimal, String)}
     *
     * @param batchQty 批量
     * @return 样本量字码（A~R）
     */
    public static String getCodeLetter(BigDecimal batchQty)
    {
        return getCodeLetter(batchQty, DEFAULT_INSPECT_LEVEL);
    }

    /**
     * 根据批量和检验水平确定样本量字码（GB/T 2828.1 表1）
     *
     * @param batchQty     批量
     * @param inspectLevel 检验水平（S-1, S-2, S-3, S-4, I, II, III）
     *                     传入 null 或无法识别的值时默认使用 II
     * @return 样本量字码（A~R）
     */
    public static String getCodeLetter(BigDecimal batchQty, String inspectLevel)
    {
        if (batchQty == null || batchQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            return "A";
        }
        String levelKey = normalizeInspectLevel(inspectLevel);
        TreeMap<Integer, String> letterMap = CODE_LETTER_MAP.get(levelKey);
        if (letterMap == null)
        {
            letterMap = CODE_LETTER_MAP.get(DEFAULT_INSPECT_LEVEL);
        }
        int qty = batchQty.intValue();
        // TreeMap.floorEntry: 找到 <= qty 的最大 key
        Map.Entry<Integer, String> entry = letterMap.floorEntry(qty);
        return entry != null ? entry.getValue() : "A";
    }

    /**
     * 将检验水平输入标准化为 CODE_LETTER_MAP 的 key。
     * 支持以下输入（大小写不敏感）：
     * S-1/S1/s1 → "S-1"，S-2/S2/s2 → "S-2"，S-3/S3/s3 → "S-3"，S-4/S4/s4 → "S-4"
     * 1/I/i → "I"，2/II/ii → "II"，3/III/iii → "III"
     * 其他/null → "II"（默认）
     */
    private static String normalizeInspectLevel(String input)
    {
        if (input == null || input.trim().isEmpty())
        {
            return DEFAULT_INSPECT_LEVEL;
        }
        String s = input.trim().toUpperCase();
        // 特殊检验水平
        if (s.equals("S-1") || s.equals("S1")) return "S-1";
        if (s.equals("S-2") || s.equals("S2")) return "S-2";
        if (s.equals("S-3") || s.equals("S3")) return "S-3";
        if (s.equals("S-4") || s.equals("S4")) return "S-4";
        // 一般检验水平
        if (s.equals("I") || s.equals("1") || s.equals("L1")) return "I";
        if (s.equals("II") || s.equals("2") || s.equals("L2")) return "II";
        if (s.equals("III") || s.equals("3") || s.equals("L3")) return "III";
        return DEFAULT_INSPECT_LEVEL;
    }

    // ============================================================
    // 计算方法
    // ============================================================

    /**
     * 计算抽样结果（默认一般检验水平 II）
     *
     * @param batchQty 批量
     * @param aqlLevel AQL等级（如 "1.0", "2.5"）
     * @return int[]{样本量n, 接收数Ac, 拒收数Re}，null表示需人工指定
     */
    public static int[] calculate(BigDecimal batchQty, String aqlLevel)
    {
        return calculate(batchQty, aqlLevel, DEFAULT_INSPECT_LEVEL);
    }

    /**
     * 计算抽样结果（指定检验水平）
     * <p>
     * 流程：
     * 1. 根据批量+检验水平 → 样本量字码（GB/T 2828.1 表1）
     * 2. 根据字码 → 样本量 n（GB/T 2828.1 表2）
     * 3. 根据字码+AQL等级 → Ac/Re（GB/T 2828.1 表3-A 正常检验主表）
     * 4. 当字码在AQL表中无直接条目时，按箭头规则向下/向上查找
     * 5. GB/T 2828.1: 当样本量 ≥ 批量时，执行100%检验（n = 批量）
     *
     * @param batchQty     批量
     * @param aqlLevel     AQL等级（如 "1.0", "2.5"）
     * @param inspectLevel 检验水平（S-1, S-2, S-3, S-4, I, II, III）
     * @return int[]{样本量n, 接收数Ac, 拒收数Re}，null表示需人工指定
     */
    public static int[] calculate(BigDecimal batchQty, String aqlLevel, String inspectLevel)
    {
        if (batchQty == null || batchQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            return null;
        }
        // 1. 确定字码
        String codeLetter = getCodeLetter(batchQty, inspectLevel);
        // 2. 根据字码确定样本量
        Integer n = SAMPLE_SIZE_MAP.get(codeLetter);
        if (n == null)
        {
            return null;
        }
        // 3. 查AQL表获取 Ac/Re
        Map<String, int[]> levelTable = AQL_TABLE.get(aqlLevel);
        if (levelTable == null)
        {
            // 默认使用 AQL 1.0
            levelTable = AQL_TABLE.get("1.0");
        }
        int[] acRe = levelTable.get(codeLetter);
        if (acRe == null)
        {
            // 字码在AQL表中无直接条目，按箭头规则查找：
            // 先尝试向下查找（↓ 箭头）：找第一个字码 >= 当前字码且在AQL表中有条目的条目
            // TreeMap 按 key 自然排序（字母序 = 样本量升序），所以遍历即为字码升序
            for (Map.Entry<String, int[]> entry : levelTable.entrySet())
            {
                String entryCode = entry.getKey();
                if (compareCodeLetter(entryCode, codeLetter) >= 0)
                {
                    int entryN = SAMPLE_SIZE_MAP.getOrDefault(entryCode, 0);
                    int batchN = batchQty.intValue();
                    acRe = entry.getValue();
                    // 关键修复：只有当查到的样本量 ≤ 批量时，才使用新字码的样本量
                    // 否则保持原字码的样本量 n（后续 100%检验截断逻辑处理）
                    if (entryN <= batchN)
                    {
                        n = entryN;
                    }
                    break;
                }
            }
            // 向下查找未命中时，向上查找（↑ 箭头）：使用表中样本量最大的条目
            if (acRe == null)
            {
                for (Map.Entry<String, int[]> entry : levelTable.entrySet())
                {
                    int entryN = SAMPLE_SIZE_MAP.getOrDefault(entry.getKey(), 0);
                    int batchN = batchQty.intValue();
                    acRe = entry.getValue();
                    if (entryN <= batchN)
                    {
                        n = entryN;
                    }
                }
            }
        }
        int ac, re;
        if (acRe == null)
        {
            ac = 0;
            re = 1;
        }
        else
        {
            ac = acRe[0];
            re = acRe[1];
        }
        // GB/T 2828.1: 当样本量 ≥ 批量时，执行100%检验（n = 批量）
        int batchN = batchQty.intValue();
        if (n >= batchN)
        {
            n = batchN;
        }
        return new int[]{n, ac, re};
    }

    /**
     * 比较两个样本量字码的大小（按样本量升序）
     * A < B < C < ... < R
     *
     * @return 负数表示 code1 < code2，0 表示相等，正数表示 code1 > code2
     */
    private static int compareCodeLetter(String code1, String code2)
    {
        Integer n1 = SAMPLE_SIZE_MAP.get(code1);
        Integer n2 = SAMPLE_SIZE_MAP.get(code2);
        int v1 = n1 != null ? n1 : Integer.MAX_VALUE;
        int v2 = n2 != null ? n2 : Integer.MAX_VALUE;
        return Integer.compare(v1, v2);
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
