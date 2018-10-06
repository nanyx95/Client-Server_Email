package client;

import utilities.Email;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Fabio Somaglia on 11/11/17.
 */

/**
 * Classe di supporto per creare una lista compatibile con JList.
 *
 * @param <T>: generico elemento
 */
public class SortedListModel<T> extends AbstractListModel {

    private List<T> model;

    public SortedListModel(List<T> list) {
        model = list;
    }

    public int getSize() {
        return model.size();
    }

    public T getElementAt(int index) {
        return model.get(index);
    }

    public void add(T element) {
        if (model.add(element)) {
            fireContentsChanged(this, 0, getSize());
        }
    }

    public void fireDataChanged() {
        int index = model.size();
        fireContentsChanged(model.get(index - 1), index, index);
    }

    public void addAll(T elements[]) {
        Collection<T> c = Arrays.asList(elements);
        model.addAll(c);
        fireContentsChanged(this, 0, getSize());
    }

    public void clear() {
        model.clear();
        fireContentsChanged(this, 0, getSize());
    }

    public int indexOf(Email email) {
        return model.indexOf(email);
    }

    public boolean removeElement(T element) {
        boolean removed = model.remove(element);
        if (removed) {
            fireContentsChanged(this, 0, getSize());
        }
        return removed;
    }

}
