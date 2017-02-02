package ru.binaryblitz.Chisto.Utils;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.util.ArrayList;

import ru.binaryblitz.Chisto.Model.Laundry;
import ru.binaryblitz.Chisto.Model.Order;
import ru.binaryblitz.Chisto.Model.Treatment;

@SuppressWarnings("unused")
public class OrderList {
    private static ArrayList<Order> orders = new ArrayList<>();
    private static ArrayList<Treatment> bufferTreatments = new ArrayList<>();
    private static Laundry laundry;
    private static int currentItem = 0;
    private static ArrayList<Pair<Integer, Double>> decorationMultipliers = new ArrayList<>();

    public static ArrayList<Pair<Integer, Double>> getDecorationMultiplier() {
        return decorationMultipliers;
    }

    public static void setDecorationMultiplier(ArrayList<Pair<Integer, Double>> decorationMultipliers) {
        OrderList.decorationMultipliers = decorationMultipliers;
    }

    public static void add(Order order) {
        orders.add(order);
        currentItem = orders.size() - 1;
    }

    public static void clear() {
        orders.clear();
        currentItem = 0;
    }

    public static void setLaundry(Laundry laundry) {
        OrderList.laundry = laundry;
    }

    public static Laundry getLaundry() {
        return laundry;
    }

    @Nullable
    public static Order get(int i) {
        if (i < orders.size()) {
            currentItem = i;
            return orders.get(i);
        } else {
            return null;
        }
    }

    @Nullable
    public static ArrayList<Order> get() {
        return orders;
    }

    public static ArrayList<Treatment> getTreatments() {
        if (currentItem < orders.size()) {
            return orders.get(currentItem).getTreatments() == null ? new ArrayList<Treatment>() : orders.get(currentItem).getTreatments();
        } else {
            return new ArrayList<>();
        }
    }

    public static ArrayList<Treatment> getAllTreatments() {
        ArrayList<Treatment> treatments = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            treatments.addAll(orders.get(i).getTreatments());
        }

        return treatments;
    }

    public static void copyToBuffer(ArrayList<Treatment> treatments) {
        bufferTreatments.clear();

        for (Treatment treatment : treatments) {
            bufferTreatments.add(treatment.copy());
        }
    }

    public static ArrayList<Treatment> getBufferTreatments() {
        if (currentItem < orders.size()) {
            return bufferTreatments;
        } else {
            return new ArrayList<>();
        }
    }

    public static void remove(int i) {
        if (i < orders.size()) {
            orders.remove(i);
        }
    }

    public static void removeCurrent() {
        if (currentItem <= orders.size() - 1) {
            orders.remove(currentItem);
        }
    }

    public static void edit(int i) {
        currentItem = i;
    }

    public static void changeCount(int count) {
        if (currentItem < orders.size()) {
            orders.get(currentItem).setCount(count);
        }
    }

    public static void changeColor(int color) {
        if (currentItem < orders.size()) {
            orders.get(currentItem).setColor(color);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void removeTreatment(int treatmentId) {
        if (currentItem >= orders.size() || orders.get(currentItem).getTreatments() == null) return;
        ArrayList<Treatment> treatments = orders.get(currentItem).getTreatments();
        for (int i = 0; i < treatments.size(); i++) {
            if (treatments.get(i).getId() == treatmentId) {
                treatments.remove(i);
                break;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void setPrice(int treatmentId, int price) {
        if (currentItem >= orders.size() || orders.get(currentItem).getTreatments() == null) return;

        for (int j = 0; j < orders.size(); j++) {
            setPriceForOrder(j, treatmentId, price);
        }
    }

    private static void setPriceForOrder(int j, int treatmentId, int price) {
        if (orders.get(j).getTreatments() == null) return;

        for (int i = 0; i < orders.get(j).getTreatments().size(); i++) {
            if (orders.get(j).getTreatments().get(i).getId() == treatmentId) {
                int decorationPrice = orders.get(j).getDecorationPrice();
                orders.get(j).setDecorationPrice(decorationPrice + ((int) ((double) price * findMultiplier(j)) - price));
                orders.get(j).getTreatments().get(i).setPrice(price);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void resetDecorationPrices() {
        if (currentItem >= orders.size() || orders.get(currentItem).getTreatments() == null) return;

        for (int i = 0; i < orders.size(); i++) {
            orders.get(i).setDecorationPrice(0);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void setSize(double size) {
        if (currentItem >= orders.size()) {
            return;
        }
        orders.get(currentItem).setSize(size);
    }

    @SuppressWarnings("ConstantConditions")
    public static void setDecorationPrice() {
        if (currentItem >= orders.size() || orders.get(currentItem).getTreatments() == null) return;

        for (int j = 0; j < orders.size(); j++) {
            setDecorationPriceForOrder(j);
        }
    }

    private static void setDecorationPriceForOrder(int index) {
        if (orders.get(index).getTreatments() == null) {
            return;
        }

        for (int i = 0; i < orders.get(index).getTreatments().size(); i++) {
            if (orders.get(index).getTreatments().get(i).getId() == AppConfig.decorationId) {
                orders.get(index).getTreatments().get(i).setPrice(orders.get(index).getDecorationPrice());
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void pullDecorationToEndOfTreatmentsList() {
        if (currentItem >= orders.size() || orders.get(currentItem).getTreatments() == null) {
            return;
        }

        for (int i = 0; i < orders.get(currentItem).getTreatments().size(); i++) {
            if (orders.get(currentItem).getTreatments().get(i).getId() == AppConfig.decorationId) {
                Treatment treatment = orders.get(currentItem).getTreatments().get(i);
                orders.get(currentItem).getTreatments().remove(i);
                orders.get(currentItem).getTreatments().add(treatment);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean isDecoration() {
        if (currentItem >= orders.size() || orders.get(currentItem).getTreatments() == null) {
            return false;
        }

        for (int i = 0; i < orders.get(currentItem).getTreatments().size(); i++) {
            if (orders.get(currentItem).getTreatments().get(i).getId() == AppConfig.decorationId) {
                return true;
            }
        }

        return false;
    }

    private static double findMultiplier(int index) {
        if (!isDecoration()) {
            return 1.0;
        }

        int id = orders.get(index).getCategory().getId();

        for (int i = 0; i < decorationMultipliers.size(); i++) {
            if (decorationMultipliers.get(i).first == id) {
                return decorationMultipliers.get(i).second;
            }
        }

        return 1.0;
    }

    @SuppressWarnings("ConstantConditions")
    public static void addTreatment(Treatment treatment) {
        if (currentItem < orders.size()) {
            return;
        }

        if (orders.get(currentItem).getTreatments() == null) {
            orders.get(currentItem).setTreatments(new ArrayList<Treatment>());
        }

        for (int i = 0; i < orders.get(currentItem).getTreatments().size(); i++) {
            if (orders.get(currentItem).getTreatments().get(i).getId() == treatment.getId()) {
                return;
            }
        }

        orders.get(currentItem).getTreatments().add(treatment);
    }

    @SuppressWarnings("ConstantConditions")
    public static void addTreatments(ArrayList<Treatment> treatments) {
        if (currentItem < orders.size()) {
            if (orders.get(currentItem).getTreatments() == null) {
                orders.get(currentItem).setTreatments(new ArrayList<Treatment>());
            }
            orders.get(currentItem).setTreatments(treatments);
        }
    }

    public static void saveTreatments(ArrayList<Treatment> treatments) {
        if (currentItem >= orders.size()) {
            return;
        }
        addTreatments(copyFromBuffer());
        bufferTreatments.clear();
    }

    private static ArrayList<Treatment> copyFromBuffer() {
        ArrayList<Treatment> res = new ArrayList<>();
        for (Treatment treatment : bufferTreatments) {
            res.add(treatment.copy());
        }

        return res;
    }

    @SuppressWarnings("ConstantConditions")
    public static void addTreatmentsForEditing(ArrayList<Treatment> treatments) {
        if (currentItem < orders.size()) {
            if (bufferTreatments == null) {
                bufferTreatments = new ArrayList<>();
            }
            bufferTreatments = treatments;
        }
    }
}
