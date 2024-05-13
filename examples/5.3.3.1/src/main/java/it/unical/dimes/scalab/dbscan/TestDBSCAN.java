//package it.unical.dimes.scalab.dbscan;
//
//import it.unical.dimes.scalab.hive.udf.DbScanUDAF;
//import it.unical.dimes.scalab.utils.GeoPoint;
//import it.unical.dimes.scalab.utils.GeoUtils;
//import it.unical.dimes.scalab.utils.KMLUtils;
//import org.json.JSONObject;
//import org.locationtech.spatial4j.shape.Shape;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//public class TestDBSCAN {
//
//    private static DbScanUDAF.DBSCANUDAFEvaluator.DbscanGeoDataset dataset = new DbScanUDAF.DBSCANUDAFEvaluator.DbscanGeoDataset();
//    private static String[] keywords = "colosseo,colis,collis,collos,Amphiteatrum Flavium,Amphitheatrum Flavium,An Colasaem,Coliseo,Coliseo,Coliseo de Roma,Coliseo de Roma,Coliseu de Roma,Coliseum,Coliseum,Coliseum,Coliseus,Colloseum,Coloseu,Colosseo,Colosseo,Colosseo,Colosseu,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Culusseu,Kolezyum,Koliseoa,Kolize,Kolizejs,Kolizey,Kolizey,Koliziejus,Kolosej,Kolosej,Koloseo,Koloseo,Koloseum,Koloseum,Koloseum,Koloseum,Koloseum,Koloseum,Koloseum,Koloseumi,Kolosseum,Kolosseum,Kolosseum,Kolosseum".split(",");
//
//    public static void main(String[] args) throws IOException {
//        List<String> points = Files.lines(
//                Paths.get("/home/lorisbel/Scrivania/Hive/hadoop-hive-spark-docker/jupyter/custom/allFlickrRome2017-Clean.json"),
//                        Charset.forName("UTF-8"))
//                .map(r -> {
//                    try {
//                        JSONObject tmp = new JSONObject(r);
//                        if (tmp.getDouble("longitude") > 0 && tmp.getDouble("latitude") > 0) {
//                            String descr = tmp.getString("description").toLowerCase();
//                            for (String k : keywords) {
//                                if (descr.contains(k)) {
//                                    return tmp.getDouble("longitude") + "," + tmp.getDouble("latitude");
//                                }
//                            }
//                            return null;
//                        }
//                        else return null;
//                    }catch (Exception e) {
//                        return null;
//                    }
//                }).filter(Objects::nonNull)
//                .collect(Collectors.toList());
//
//        points.forEach(p -> {
//            String[] data = p.split(",");
//            double x = Double.parseDouble(data[0]);
//            double y = Double.parseDouble(data[1]);
//            dataset.getPoints().add(new GeoPoint(x,y));
//        });
//
//        dataset.setEps(50);
//        dataset.setMinPts(150);
//        DbScanUDAF.DBSCANUDAFEvaluator udaf = new DbScanUDAF.DBSCANUDAFEvaluator();
//        udaf.setDataset(dataset);
//        String kml = udaf.terminate();
//        System.out.println(kml);
//
////        ElkiDBSCAN dbscan = new ElkiDBSCAN(dataset.getDistinctPoints().stream().collect(Collectors.toList()),
////                50, 150);
////        dbscan.cluster();
////        List<GeoCluster> clusters = dbscan.getAllGeoClusters(false);
////        int max = -1;
////        GeoCluster maxCluster = null;
////        for (GeoCluster c : clusters) {
////            if (c.getPoints().size() > max) {
////                maxCluster = c;
////                max = c.getPoints().size();
////            }
////        }
////        try {
////            if (maxCluster != null) {
////                Shape clusterShape = GeoUtils.convexHull(maxCluster.getPoints());
////                System.out.println(KMLUtils.serialize(clusterShape));
////            }
////            else System.out.println("NOT_FOUND");
////        } catch (IOException e) {
////            System.out.println("KML_ERROR");
////        }
//    }
//}
