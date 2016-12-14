package ru.binaryblitz.Chisto.Utils;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import ru.binaryblitz.Chisto.Model.Order;
import ru.binaryblitz.Chisto.Model.Treatment;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class OrderList {
    private static ArrayList<Order> orders = new ArrayList<>();
    private static ArrayList<Treatment> bufferTreatments = new ArrayList<>();
    private static int laundryId = 0;
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

    public static void setLaundryId(int laundryId) {
        OrderList.laundryId = laundryId;
    }

    public static int getLaundryId() {
        return laundryId;
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

    public static void copyToBuffer(ArrayList<Treatment> treatments) {
        bufferTreatments.clear();

        for(Treatment treatment: treatments) bufferTreatments.add(treatment.copy());
    }

    public static ArrayList<Treatment> getBufferTreatments() {
        if (currentItem < orders.size()) return bufferTreatments;
        else return new ArrayList<>();
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
    public static void setCost(int treatmentId, int cost) {
        if (currentItem >= orders.size() || orders.get(currentItem).getTreatments() == null) return;

        for (int i = 0; i < orders.get(currentItem).getTreatments().size(); i++) {
            if (orders.get(currentItem).getTreatments().get(i).getId() == treatmentId) {
                orders.get(currentItem).getTreatments().get(i).setCost((int) ((double) cost * findMultiplier()));
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean isDecoration() {
        if (currentItem >= orders.size() || orders.get(currentItem).getTreatments() == null) return false;

        for (int i = 0; i < orders.get(currentItem).getTreatments().size(); i++) {
            if (orders.get(currentItem).getTreatments().get(i).getId() == -1) {
                return true;
            }
        }

        return false;
    }

    private static double findMultiplier() {
        if (!isDecoration()) return 1.0;

        int id = orders.get(currentItem).getCategory().getId();

        for (int i = 0; i < decorationMultipliers.size(); i++) {
            if (decorationMultipliers.get(i).first == id) return decorationMultipliers.get(i).second;
        }

        return 1.0;
    }

    @SuppressWarnings("ConstantConditions")
    public static void addTreatment(Treatment treatment) {
        if (currentItem < orders.size()) return;

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
        if (currentItem >= orders.size()) return;
        addTreatments(copyFromBuffer());
        bufferTreatments.clear();
    }

    private static ArrayList<Treatment> copyFromBuffer() {
        ArrayList<Treatment> res = new ArrayList<>();
        for(Treatment treatment: bufferTreatments) res.add(treatment.copy());

        return res;
    }

    @SuppressWarnings("ConstantConditions")
    public static void addTreatmentsForEditing(ArrayList<Treatment> treatments) {
        if (currentItem < orders.size()) {
            if (bufferTreatments == null) bufferTreatments = new ArrayList<>();
            bufferTreatments = treatments;
        }
    }
}
