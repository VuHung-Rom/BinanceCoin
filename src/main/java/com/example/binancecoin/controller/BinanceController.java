package com.example.binancecoin.controller;


import com.example.binancecoin.dto.*;
import com.example.binancecoin.entity.CacheDataEntity;
import com.example.binancecoin.repository.CacheDataRepository;
import com.example.binancecoin.repository.DepthRepository;
import com.example.binancecoin.service.BinanceClient;
import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.*;

@RestController
public class BinanceController {

    private static Map<String, List<Cachedata>> MAP_CACHE_VOL_DATA = null;
    @Autowired
    private CacheDataRepository cacheDataRepository;


    @GetMapping("/api/allow-token")
    public ResponseEntity<?> getToken() {
        BinanceClient binanceClient = Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logger(new Slf4jLogger(BinanceClient.class))
                .logLevel(Logger.Level.FULL)
                .target(BinanceClient.class, "https://www.binance.com");

        ProductResponse response = binanceClient.getProducts();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/api/v3/ticker/{coin}")
    public ResponseEntity<GetPice24h> getTicker(@PathVariable String coin) {
        BinanceClient binanceClient = Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logger(new Slf4jLogger(BinanceClient.class))
                .logLevel(Logger.Level.FULL)
                .target(BinanceClient.class, "https://www.binance.com");
        GetPice24h getPice24hs = binanceClient.getMarket24H(coin);
        getPice24hs.setMessage("Completed");
        getPice24hs.setCode("0000");
        return ResponseEntity.status(HttpStatus.OK).body(getPice24hs);

    }

    @GetMapping("/api/v3/kline")
    public ResponseEntity<List<Cachedata>> getHistory(@RequestParam String coin, String dataRange) {
        int totalRow = 2;
        if (MAP_CACHE_VOL_DATA == null) {
            MAP_CACHE_VOL_DATA = new HashMap<>();
            // TODO: lần đầu tiên thì load du lieu tu DB len

            //Lay danh sach ca ban ghi trong db ra
            List<CacheDataEntity> lstData =
                    cacheDataRepository.findAll(Sort.by(Sort.Direction.ASC, "token")
                            .and(Sort.by(Sort.Direction.ASC, "openTime")));

            //Voi tung ban ghi tu db
            for (CacheDataEntity cacheDataEntity : lstData) {
                List<Cachedata> lst = MAP_CACHE_VOL_DATA.get(cacheDataEntity.getToken());
                //Neu ma coin chua co list trong MAP_CACHE_VOL_DATA thi can khoi tao list moi
                if (lst == null) {
                    lst = new ArrayList<>();
                    MAP_CACHE_VOL_DATA.put(cacheDataEntity.getToken(), lst);
                }
                //Neu ma coin da co list trong MAP_CACHE_VOL_DATA thi lay list ra va them vao
                Cachedata cachedata = new Cachedata();
                BeanUtils.copyProperties(cacheDataEntity, cachedata);
                lst.add(cachedata);

            }

        }

        List<Cachedata> listData = MAP_CACHE_VOL_DATA.get(coin);

        if (listData != null && new Date().getTime() - listData.get(listData.size() - 1).getCloseTime().getTime() < 300000)
            return ResponseEntity.status(HttpStatus.OK).body(listData);

        if (listData == null) {
            listData = new ArrayList<>();
            MAP_CACHE_VOL_DATA.put(coin, listData);
            totalRow = 1000;
        }

        BinanceClient binanceClient = Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logger(new Slf4jLogger(BinanceClient.class))
                .logLevel(Logger.Level.FULL)
                .target(BinanceClient.class, "https://www.binance.com");

        List<List<String>> binanceDataChart = binanceClient.getHistory(totalRow, coin, "5m");

        for (int i = 0; i < binanceDataChart.size() - 1; i++) {
            List<String> dataPoint = binanceDataChart.get(i);
            Cachedata cahedata = new Cachedata();
            cahedata.setVolume(Double.valueOf(dataPoint.get(5)));
            cahedata.setOpenTime(new Date(Long.valueOf(dataPoint.get(0))));
            cahedata.setCloseTime(new Date(Long.valueOf(dataPoint.get(6))));
            if (listData.size() == 0 || cahedata.getOpenTime().after(listData.get(listData.size() - 1).getOpenTime())) {
                listData.add(cahedata);
                //TODO: save du lieu vao db
                CacheDataEntity cacheDataEntity = new CacheDataEntity();
                BeanUtils.copyProperties(cahedata, cacheDataEntity);
                cacheDataEntity.setToken(coin.toUpperCase());
                cacheDataRepository.save(cacheDataEntity);
            }
        }


        return ResponseEntity.status(HttpStatus.OK).body(listData);

    }


    @GetMapping("/api/v3/deth")
    public ResponseEntity<?> getDeth(@RequestParam String coin) {
        DepthVol depthVol = new DepthVol();
        int totalRow = 5000;
        // TODO: cache lại dữ liệu

        BinanceClient binanceClient = Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logger(new Slf4jLogger(BinanceClient.class))
                .logLevel(Logger.Level.FULL)
                .target(BinanceClient.class, "https://www.binance.com");
        GetDepthRespose binanceClientDepth = binanceClient.getDepth(coin, totalRow);

        if (binanceClientDepth != null) {
            //TODO:
            // Tính tổng vol +5%, -5%

            Double max = null;
            Double min = null;
            for (Double price : binanceClientDepth.getAsks().keySet()) {
                if (min == null) {
                    min = price;
                } else {
                    if (min > price) {
                        min = price;
                    }
                }

            }
            for (Double price : binanceClientDepth.getBids().keySet()) {
                if (max == null) {
                    max = price;
                } else {
                    if (max < price) {
                        max = price;

                    }
                }
            }
            Double matchPrice = (max + min) / 2;

            Double fivebuy = 0D;
            for (Double price : binanceClientDepth.getAsks().keySet()) {
                Double vol = binanceClientDepth.getAsks().get(price);
                if (price < (1.05 * matchPrice)) {
                    fivebuy += vol;
                }
            }
            Double haibuy = 0D;
            for (Double price : binanceClientDepth.getAsks().keySet()) {
                Double vol = binanceClientDepth.getAsks().get(price);
                if (price < (1.02 * matchPrice)) {
                    haibuy += vol;
                }
            }

            Double fiveSell = 0D;
            for (Double price : binanceClientDepth.getBids().keySet()) {
                Double vol = binanceClientDepth.getBids().get(price);
                if (price > (0.95 * matchPrice)) {
                    fiveSell += vol;
                }
            }
            Double haiSell = 0D;
            for (Double price : binanceClientDepth.getBids().keySet()) {
                Double vol = binanceClientDepth.getBids().get(price);
                if (price > (0.98 * matchPrice)) {
                    haiSell += vol;
                }
            }
            depthVol.setFiveBuy(fivebuy);
            depthVol.setFiveSell(fiveSell);
            depthVol.setHaiBuy(haibuy);
            depthVol.setHaiSell(haiSell);

            return ResponseEntity.status(HttpStatus.OK).body(depthVol);
        } else {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/api/v3/deth-test")
    public ResponseEntity<?> getDethTest(@RequestParam String coin) {
        DepthVol depthVol = new DepthVol();
        int totalRow = 5000;
        // TODO: cache lại dữ liệu

        BinanceClient binanceClient = Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logger(new Slf4jLogger(BinanceClient.class))
                .logLevel(Logger.Level.FULL)
                .target(BinanceClient.class, "https://www.binance.com");
        GetDepthRespose binanceClientDepth = binanceClient.getDepth(coin, totalRow);

        if (binanceClientDepth != null) {
            /**
             *
             Tìm giá trị lớn thứ 2 trong mảng BIDS
             Tìm giá trị bé thứ 2 trong mảng BIDS
             Tính giá trị trung bình của khối lượng giao dịch (bids, ask).
             Kiểm tra xem có giá nhập vào ko: trả về "CÓ" hoặc  "KHÔNG"
             Kiểm tra xem có giá trị trùng không
             kiểm tra danh sách giá trị trùng ở 2 mảng
             Lấy danh sách các phần tử nhỏ hơn giá truyền vào bids
             Lấy danh sách các phần tử lớn hơn giá truyền vào ask
             Giá trung bình của các phần tử lớn hơn giá truyền vào ask
             Tính khối lượng trung bình của các khoảng giá trừ 2 khoảng min và max của bids

             */
            Map<Double, Double> bids = binanceClientDepth.getBids();
            Double secondMax = giaTriLonThu2(bids);
            Double scondMin = giaTriBeThu2(bids);
            Map<Double, Double> orderBook = binanceClientDepth.getBids();
            Double tbVolume = trungBinhKhoiLuong(orderBook);
            Double giaNhap = 200D;
            Boolean kt = tonTaiGia(orderBook, giaNhap);
            Map<Double, Double> dsnhohon = phanTuGiaNhoHon(orderBook, giaNhap);
            Map<Double, Double> dslonhon = phanTuGiaLonHon(orderBook, giaNhap);
            Boolean giatrung = coGiaTrung(orderBook);
            Double tbVolumeTruMinMax = khoiLuongTrungBinhLoaiTru(orderBook);
            return ResponseEntity.status(HttpStatus.OK).body(dslonhon);
        /*    if (kt==true) {
                return ResponseEntity.status(HttpStatus.OK).body("Giá tồn tại trong orderBook.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Giá không tồn tại trong orderBook.");
            }*/
        } else {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Tìm giá trị lớn thứ 2 trong mảng
     *
     * @param bids
     * @return
     */
    private Double giaTriLonThu2(Map<Double, Double> bids) {
        Double max2 = null;
        Double secondMax = null;
        for (Double price : bids.keySet()) {
            if (max2 == null || price >= max2) {
                secondMax = max2;

                max2 = price;
            } else if (secondMax == null || price > secondMax) {
                secondMax = price;
            }
        }

        return secondMax;
    }

    /**
     * Tìm giá trị bé thứ 2 trong mảng
     *
     * @param bids
     * @return
     */
    private Double giaTriBeThu2(Map<Double, Double> bids) {
        Double min2 = null;
        Double secondMin = null;
        for (Double price : bids.keySet()) {
            if (min2 == null || price <= min2) {
                secondMin = min2;

                min2 = price;
            } else if (secondMin == null || price < secondMin) {
                secondMin = price;
            }
        }

        return secondMin;
    }


    /**
     * Tính giá trị trung bình của khối lượng giao dịch
     *
     * @param orderBook
     * @return
     */
    private Double trungBinhKhoiLuong(Map<Double, Double> orderBook) {

        Double tongVol = 0D;
        int count = 0;
        for (Double vol : orderBook.values()) {
            count++;
            tongVol += vol;
        }

        return tongVol / count;

    }

    /**
     * Kiểm tra xem có giá nhập vào ko: trả về "CÓ" hoặc  "KHÔNG"
     *
     * @param orderBook
     * @param price
     * @return
     */
    private Boolean tonTaiGia(Map<Double, Double> orderBook, Double price) {
        return orderBook.containsKey(price);
    }

    /**
     * kiểm tra danh sách giá trị trùng ở 2 mảng
     *
     * @param orderBook
     * @return
     */
    private Boolean coGiaTrung(Map<Double, Double> orderBook) {
        Set<Double> giaTrung = new HashSet<>();
        for (Map.Entry<Double, Double> vol : orderBook.entrySet()) {
            Double giaTri = vol.getValue();
            if (giaTrung.contains(giaTri)) {
                return true;
            }
            giaTrung.add(giaTri);
        }

        return false;
    }

    /**
     * Lấy danh sách các phần tử nhỏ hơn giá truyền vào bids
     *
     * @param orderBook
     * @return
     */
    private Map<Double, Double> phanTuGiaNhoHon(Map<Double, Double> orderBook, Double price) {

        Map<Double, Double> danhSach = new HashMap<>();

        for (Map.Entry<Double, Double> entry : orderBook.entrySet()) {
            Double key = entry.getKey();
            Double value = entry.getValue();

            if (price > key) {

                danhSach.put(key, value);
            }
        }

        return danhSach;
    }

    /**
     * Lấy danh sách các phần tử lớn hơn giá truyền vào ask
     *
     * @param orderBook
     * @param price
     * @return
     */
    private Map<Double, Double> phanTuGiaLonHon(Map<Double, Double> orderBook, Double price) {
        Map<Double, Double> danhSach = new HashMap<>();

        for (Map.Entry<Double, Double> entry : orderBook.entrySet()) {
            Double key = entry.getKey();
            Double value = entry.getValue();

            if (price < key) {

                danhSach.put(key, value);
            }
        }

        return danhSach;
    }


    /**
     * Tính khối lượng trung bình của các khoảng giá trừ 2 khoảng min và max của bids
     *
     * @param orderBook
     * @return
     */
    private Double khoiLuongTrungBinhLoaiTru(Map<Double, Double> orderBook) {
        Double max = null;
        Double min = null;
        for (Double price : orderBook.values()) {
            if (min == null) {
                min = price;
            } else {
                if (min > price) {
                    min = price;
                }
            }

        }
        for (Double price : orderBook.values()) {
            if (max == null) {
                max = price;
            } else {
                if (max < price) {
                    max = price;

                }
            }
        }
        Double tongVol = 0D;
        int count = 0;
        for (Double vol : orderBook.values()) {
            count++;
            tongVol += vol;
        }

        count = count - 2;
        tongVol = tongVol - (max - min);

        return tongVol / count;
    }

    @GetMapping("/api/v3/kline-practice")
    public ResponseEntity<?> getKlinePractice(@RequestParam String coin, String dataRange) {

        List<Cachedata> listData = getKlineData(coin);
        /**
         * -- Kline
         *  Lấy Tổng khối lượng giao dịch trong 2 ngày gần nhất
         *  Lấy Tổng khối lượng giao dịch trong 1 ngày gần nhất
         *  Lấy khối lượng giao dịch trung bình theo giờ của 1 ngày gần nhất
         *  Lấy danh sách khối lượng giao dịch theo giờ của 2 ngày gần nhất (trả về mỗi giờ 1 giá trị, khối lượng sẽ là tổng khối lượng trong khung giờ đấy tính)
         */


        Double khoiluong1ngay = KhoiLuong1Ngay(listData);
        Double khoiluong2ngay = KhoiLuong2Ngay(listData);
        Double khoiluongTB = KhoiLuongTrungBinhGio1Ngay(listData);
        List<Cachedata> kl = dataTheoGio1Ngay(listData);

        return ResponseEntity.status(HttpStatus.OK).body(kl);

    }

    private Double KhoiLuong2Ngay(List<Cachedata> listData) {
        Long tgHaiNgayTruoc = new Date().getTime() - (2 * 60 * 60 * 24 * 1000);
        Double tongVol = 0D;
        for (Cachedata data : listData) {
            if (data.getOpenTime().getTime() >= tgHaiNgayTruoc) {
                tongVol += data.getVolume();
            }
        }

        return tongVol;
    }

    private Double KhoiLuong1Ngay(List<Cachedata> listData) {

        Long tgMotNgayTruoc = new Date().getTime() - (60 * 60 * 24 * 1000);
        Double tongVol = 0D;
        for (Cachedata data : listData) {
            if (data.getOpenTime().getTime() >= tgMotNgayTruoc) {
                tongVol += data.getVolume();
            }
        }

        return tongVol;
    }

    private Double KhoiLuongTrungBinhGio1Ngay(List<Cachedata> listData) {

        Long tgMotNgayTruoc = new Date().getTime() - (60 * 60 * 24 * 1000);
        Double tongVol = 0D;
        int count = 0;
        for (Cachedata cachedata : listData) {
            if (cachedata.getOpenTime().getTime() >= tgMotNgayTruoc) {
                count++;
                tongVol += cachedata.getVolume();
            }
        }
        return tongVol / count;
    }

    private List<Cachedata> dataTheoGio1Ngay(List<Cachedata> listData) {
        List<Cachedata> danhSachvol1h = new ArrayList<>();
        Long tg2NgayTruoc = new Date().getTime() - (2 * 60 * 60 * 24 * 1000);
        Map<Long, Double> dulieu1h = new HashMap<>();

        for (Cachedata cachedata : listData) {
            if (cachedata.getOpenTime().getTime() >= tg2NgayTruoc) {
                Long Gio= (cachedata.getOpenTime().getTime()/(60 * 60 * 1000))*(60 * 60 * 1000);
                dulieu1h.put(Gio,dulieu1h.getOrDefault(Gio,0.0) + cachedata.getVolume());

            }
        }
        for(Map.Entry<Long,Double> entry: dulieu1h.entrySet()){
            Cachedata giodata = new Cachedata();
            giodata.setOpenTime(new Date(entry.getKey()));
            giodata.setVolume(entry.getValue());
            danhSachvol1h.add(giodata);

        }


        return danhSachvol1h;
    }

    private List<Cachedata> getKlineData(String coin) {
        int totalRow = 2;
        if (MAP_CACHE_VOL_DATA == null) {
            MAP_CACHE_VOL_DATA = new HashMap<>();
            // TODO: lần đầu tiên thì load du lieu tu DB len

            //Lay danh sach ca ban ghi trong db ra
            List<CacheDataEntity> lstData =
                    cacheDataRepository.findAll(Sort.by(Sort.Direction.ASC, "token")
                            .and(Sort.by(Sort.Direction.ASC, "openTime")));

            //Voi tung ban ghi tu db
            for (CacheDataEntity cacheDataEntity : lstData) {
                List<Cachedata> lst = MAP_CACHE_VOL_DATA.get(cacheDataEntity.getToken());
                //Neu ma coin chua co list trong MAP_CACHE_VOL_DATA thi can khoi tao list moi
                if (lst == null) {
                    lst = new ArrayList<>();
                    MAP_CACHE_VOL_DATA.put(cacheDataEntity.getToken(), lst);
                }
                //Neu ma coin da co list trong MAP_CACHE_VOL_DATA thi lay list ra va them vao
                Cachedata cachedata = new Cachedata();
                BeanUtils.copyProperties(cacheDataEntity, cachedata);
                lst.add(cachedata);

            }

        }

        List<Cachedata> listData = MAP_CACHE_VOL_DATA.get(coin);

        if (listData != null && new Date().getTime() - listData.get(listData.size() - 1).getCloseTime().getTime() < 300000)
            return listData;

        if (listData == null) {
            listData = new ArrayList<>();
            MAP_CACHE_VOL_DATA.put(coin, listData);
            totalRow = 1000;
        }

        BinanceClient binanceClient = Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logger(new Slf4jLogger(BinanceClient.class))
                .logLevel(Logger.Level.FULL)
                .target(BinanceClient.class, "https://www.binance.com");

        List<List<String>> binanceDataChart = binanceClient.getHistory(totalRow, coin, "5m");

        for (int i = 0; i < binanceDataChart.size() - 1; i++) {
            List<String> dataPoint = binanceDataChart.get(i);
            Cachedata cahedata = new Cachedata();
            cahedata.setVolume(Double.valueOf(dataPoint.get(5)));
            cahedata.setOpenTime(new Date(Long.valueOf(dataPoint.get(0))));
            cahedata.setCloseTime(new Date(Long.valueOf(dataPoint.get(6))));
            if (listData.size() == 0 || cahedata.getOpenTime().after(listData.get(listData.size() - 1).getOpenTime())) {
                listData.add(cahedata);
                //TODO: save du lieu vao db
                CacheDataEntity cacheDataEntity = new CacheDataEntity();
                BeanUtils.copyProperties(cahedata, cacheDataEntity);
                cacheDataEntity.setToken(coin.toUpperCase());

                cacheDataRepository.save(cacheDataEntity);
            }
        }
        return listData;
    }
}



