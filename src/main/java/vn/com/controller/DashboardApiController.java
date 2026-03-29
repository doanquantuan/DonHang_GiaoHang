package vn.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.entity.Delivery;
import vn.com.entity.Order;
import vn.com.enums.DeliveryStatus;
import vn.com.enums.OrderStatus;
import vn.com.repository.DeliveryRepository;
import vn.com.repository.OrderRepository;
import vn.com.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardApiController {

    @Autowired private OrderRepository    orderRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private UserRepository     userRepository;

    /**
     * GET /api/dashboard/stats?from=2024-01-01&to=2024-12-31
     *
     * from / to    : ISO date, tuỳ chọn. Mặc định = 6 tháng gần nhất
     * granularity  : tự động — ≤31 ngày → theo ngày; ≤92 ngày → theo tuần; >92 ngày → theo tháng
     * KPI cards    : luôn phản ánh dữ liệu trong khoảng from–to đã chọn
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime now = LocalDateTime.now();
        if (to   == null) to   = now.toLocalDate();
        if (from == null) from = to.minusMonths(5).withDayOfMonth(1);

        LocalDateTime rangeStart = from.atStartOfDay();
        LocalDateTime rangeEnd   = to.plusDays(1).atStartOfDay();

        List<Order>    allOrders     = orderRepository.findAll();
        List<Delivery> allDeliveries = deliveryRepository.findAll();

        // Lọc theo range
        List<Order> rangeOrders = allOrders.stream()
                .filter(o -> o.getOrderDate() != null
                        && !o.getOrderDate().isBefore(rangeStart)
                        && o.getOrderDate().isBefore(rangeEnd))
                .collect(Collectors.toList());

        List<Delivery> rangeDeliveries = allDeliveries.stream()
                .filter(d -> {
                    Order o = d.getOrder();
                    return o != null && o.getOrderDate() != null
                            && !o.getOrderDate().isBefore(rangeStart)
                            && o.getOrderDate().isBefore(rangeEnd);
                })
                .collect(Collectors.toList());

        // ── KPI ────────────────────────────────────────────────────
        long totalOrders = rangeOrders.size();
        long delivering  = rangeDeliveries.stream().filter(d -> d.getStatus() == DeliveryStatus.DELIVERING).count();
        long done        = rangeDeliveries.stream().filter(d -> d.getStatus() == DeliveryStatus.DONE).count();
        long failed      = rangeOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

        LocalDateTime startOfDay       = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth     = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);

        long newToday  = allOrders.stream()
                .filter(o -> o.getOrderDate() != null && !o.getOrderDate().isBefore(startOfDay)).count();
        long doneToday = allDeliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DONE
                        && d.getDeliveryDate() != null
                        && !d.getDeliveryDate().isBefore(startOfDay)).count();

        long thisMonthCnt = allOrders.stream()
                .filter(o -> o.getOrderDate() != null && !o.getOrderDate().isBefore(startOfMonth)).count();
        long lastMonthCnt = allOrders.stream()
                .filter(o -> o.getOrderDate() != null
                        && !o.getOrderDate().isBefore(startOfLastMonth)
                        && o.getOrderDate().isBefore(startOfMonth)).count();

        double growthPct = lastMonthCnt == 0 ? 0
                : Math.round(((double)(thisMonthCnt - lastMonthCnt) / lastMonthCnt) * 1000.0) / 10.0;
        double doneRate  = totalOrders == 0 ? 0
                : Math.round((double) done / totalOrders * 1000.0) / 10.0;

        // ── Granularity ─────────────────────────────────────────────
        long daysDiff = to.toEpochDay() - from.toEpochDay() + 1;
        String granularity = daysDiff <= 31 ? "day" : daysDiff <= 92 ? "week" : "month";

        // ── Biểu đồ cột + đường theo granularity ────────────────────
        List<Map<String, Object>> monthlyOrders  = new ArrayList<>();
        List<Map<String, Object>> monthlyRevenue = new ArrayList<>();

        if ("day".equals(granularity)) {
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                LocalDate fd = d;
                long cnt = rangeOrders.stream()
                        .filter(o -> o.getOrderDate().toLocalDate().equals(fd)).count();
                double rev = rangeOrders.stream()
                        .filter(o -> o.getOrderDate().toLocalDate().equals(fd)
                                && o.getStatus() == OrderStatus.COMPLETED
                                && o.getTotalAmount() != null)
                        .mapToDouble(Order::getTotalAmount).sum();
                String lbl = fd.getDayOfMonth() + "/" + fd.getMonthValue();
                monthlyOrders.add(barEntry(lbl, cnt));
                monthlyRevenue.add(revEntry(lbl, rev));
            }
        } else if ("week".equals(granularity)) {
            LocalDate cursor = from;
            while (!cursor.isAfter(to)) {
                LocalDate wEnd = cursor.plusDays(6).isAfter(to) ? to : cursor.plusDays(6);
                LocalDateTime ws = cursor.atStartOfDay();
                LocalDateTime we = wEnd.plusDays(1).atStartOfDay();
                long cnt = rangeOrders.stream()
                        .filter(o -> !o.getOrderDate().isBefore(ws) && o.getOrderDate().isBefore(we)).count();
                double rev = rangeOrders.stream()
                        .filter(o -> !o.getOrderDate().isBefore(ws) && o.getOrderDate().isBefore(we)
                                && o.getStatus() == OrderStatus.COMPLETED && o.getTotalAmount() != null)
                        .mapToDouble(Order::getTotalAmount).sum();
                String lbl = cursor.getDayOfMonth() + "/" + cursor.getMonthValue()
                        + "–" + wEnd.getDayOfMonth() + "/" + wEnd.getMonthValue();
                monthlyOrders.add(barEntry(lbl, cnt));
                monthlyRevenue.add(revEntry(lbl, rev));
                cursor = cursor.plusDays(7);
            }
        } else {
            LocalDate cursor = from.withDayOfMonth(1);
            while (!cursor.isAfter(to.withDayOfMonth(1))) {
                LocalDateTime ms = cursor.atStartOfDay();
                LocalDateTime me = cursor.plusMonths(1).atStartOfDay();
                long cnt = rangeOrders.stream()
                        .filter(o -> !o.getOrderDate().isBefore(ms) && o.getOrderDate().isBefore(me)).count();
                double rev = rangeOrders.stream()
                        .filter(o -> !o.getOrderDate().isBefore(ms) && o.getOrderDate().isBefore(me)
                                && o.getStatus() == OrderStatus.COMPLETED && o.getTotalAmount() != null)
                        .mapToDouble(Order::getTotalAmount).sum();
                monthlyOrders.add(barEntry("T" + cursor.getMonthValue(), cnt));
                monthlyRevenue.add(revEntry("T" + cursor.getMonthValue(), rev));
                cursor = cursor.plusMonths(1);
            }
        }

        // ── Donut ───────────────────────────────────────────────────
        long cNew       = rangeOrders.stream().filter(o -> o.getStatus() == OrderStatus.NEW).count();
        long cConfirmed = rangeOrders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count();
        long cCompleted = rangeOrders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        long cCancelled = rangeOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

        List<Map<String, Object>> orderStatus = List.of(
                statusEntry("Thành công", cCompleted, totalOrders),
                statusEntry("Đang giao",  cConfirmed, totalOrders),
                statusEntry("Chờ xử lý",  cNew,       totalOrders),
                statusEntry("Đã hủy",     cCancelled, totalOrders));

        // ── Top Shipper ─────────────────────────────────────────────
        Map<String, Long> shipperMap = rangeDeliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DONE && d.getShipperName() != null)
                .collect(Collectors.groupingBy(Delivery::getShipperName, Collectors.counting()));

        List<Map<String, Object>> topShippers = shipperMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("name",  e.getKey());
                    s.put("count", e.getValue());
                    long tot = rangeDeliveries.stream()
                            .filter(d -> e.getKey().equals(d.getShipperName())).count();
                    s.put("successRate", tot == 0 ? 0
                            : Math.round((double) e.getValue() / tot * 1000.0) / 10.0);
                    return s;
                })
                .collect(Collectors.toList());

        // ── Recent orders ───────────────────────────────────────────
        List<Map<String, Object>> recentOrders = rangeOrders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(10)
                .map(o -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id",           "DH-" + String.format("%04d", o.getId()));
                    row.put("orderId",      o.getId());
                    row.put("customerName", o.getCustomerName());
                    row.put("address",      o.getAddress());
                    row.put("orderDate",    o.getOrderDate().toLocalDate().toString());
                    row.put("status",       o.getStatus() != null ? o.getStatus().name() : "NEW");
                    row.put("totalAmount",  o.getTotalAmount());
                    return row;
                })
                .collect(Collectors.toList());

        // ── Response ────────────────────────────────────────────────
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalOrders",    totalOrders);
        result.put("delivering",     delivering);
        result.put("done",           done);
        result.put("failed",         failed);
        result.put("newToday",       newToday);
        result.put("doneToday",      doneToday);
        result.put("growthPct",      growthPct);
        result.put("doneRate",       doneRate);
        result.put("granularity",    granularity);
        result.put("from",           from.toString());
        result.put("to",             to.toString());
        result.put("monthlyOrders",  monthlyOrders);
        result.put("monthlyRevenue", monthlyRevenue);
        result.put("orderStatus",    orderStatus);
        result.put("topShippers",    topShippers);
        result.put("recentOrders",   recentOrders);
        result.put("totalShippers",  userRepository.findByRoleAndIsActive("SHIPPER", true).size());
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> barEntry(String label, long count) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("month", label); m.put("count", count); return m;
    }
    private Map<String, Object> revEntry(String label, double rev) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("month",   label);
        m.put("revenue", Math.round(rev / 1_000.0) / 1_000.0); return m;
    }
    private Map<String, Object> statusEntry(String label, long count, long total) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("label", label);
        m.put("count", count);
        m.put("pct",   total == 0 ? 0 : Math.round((double) count / total * 1000.0) / 10.0);
        return m;
    }
}