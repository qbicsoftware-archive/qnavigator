package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class spaceToProjectPrefixMap {
  public static final Map<String, String> myMap;
  static {
    Map<String, String> aMap = new HashMap<String, String>();
    aMap.put("IVAC_ALL", "QA");
    aMap.put("IVAC_CEGAT", "QC");
    aMap.put("IVAC_TEST_SPACE", "QT");
    aMap.put("IVAC_HEPA_VAC", "QH");
    aMap.put("IVAC_INDIVIDUAL_LIVER", "QI");
    aMap.put("IVAC_SFB685_C9_PC", "QS");
    aMap.put("IVAC_ALL_DKTK", "QD");
    aMap.put("IVAC_AML_KIKLI", "QAK");
    aMap.put("IVAC_EWING", "QE");
    aMap.put("IVAC_INFORM_DKTK_KIKLI", "QFK");
    aMap.put("IVAC_LUCA", "QL");
    aMap.put("IVAC_PANC", "QP");
    aMap.put("IVAC_PANC_KIKLI", "QPK");
    aMap.put("IVAC_RCC", "QR");
    aMap.put("IVAC_RCC_KIKLI", "QRK");
    aMap.put("IVAC_SARC", "QS");
    aMap.put("IVAC_MACA", "QB");
    aMap.put("IVAC_OVCA", "QO");
    myMap = Collections.unmodifiableMap(aMap);
  }
}
