package com.chisto.Utils;

import com.chisto.Model.Order;
import com.chisto.Model.Treatment;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class OrderList {
    private static ArrayList<Order> orders = new ArrayList<>();

    private static int currentItem = 0;

    public static void add(Order order) {
        orders.add(order);
        currentItem = orders.size() - 1;
    }

    public static void clear() {
        orders.clear();
        currentItem = 0;
    }

    public static Order get(int i) {
        return orders.get(i);
    }

    public static ArrayList<Order> get() {
        return orders;
    }

    public static void remove(int i) {
        orders.remove(i);
    }

    public static void removeCurrent() {
        orders.remove(currentItem);
    }

    public static void edit(int i) {
        currentItem = i;
    }

    public static void change(int count) {
        orders.get(currentItem).setCount(count);
    }

    @SuppressWarnings("ConstantConditions")
    public static void removeTreatment(int treatmentId) {
        if(orders.get(currentItem).getTreatments() != null) {
            for(int i = 0; i < orders.get(currentItem).getTreatments().size(); i++) {
                if(orders.get(currentItem).getTreatments().get(i).getId() == treatmentId) {
                    orders.get(currentItem).getTreatments().remove(i);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void addTreatment(Treatment treatment) {
        if(orders.get(currentItem).getTreatments() == null) {
            orders.get(currentItem).setTreatments(new ArrayList<Treatment>());
        }
        orders.get(currentItem).getTreatments().add(treatment);
    }

    @SuppressWarnings("ConstantConditions")
    public static void addTreatments(ArrayList<Treatment> treatments) {
        if(orders.get(currentItem).getTreatments() == null) {
            orders.get(currentItem).setTreatments(new ArrayList<Treatment>());
        }
        orders.get(currentItem).getTreatments().addAll(treatments);
    }
}
