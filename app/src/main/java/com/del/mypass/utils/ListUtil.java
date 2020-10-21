package com.del.mypass.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by dodolinel
 * date: 12.12.14
 * <p/>
 * Утилиты для работы со списками
 */
public class ListUtil {

    /**
     * Удаляет элементы из списка
     *
     * @param list      исходный список
     * @param predicate условие удаления
     * @return кол-во удаленных записей
     */
    public static <T> int remove(List<T> list, Predicate<? super T> predicate) {
        int count = 0;
        for (Iterator<T> it = list.iterator(); it.hasNext(); ) {
            T obj = it.next();
            if (predicate.test(obj)) {
                count++;
                it.remove();
            }
        }
        return count;
    }

    /**
     * Разбить список на подсписки фиксированной максимальной длинны
     *
     * @param l           список
     * @param maxListSize максимальный размер подсписка
     */
    public static <E> List<List<E>> split(final int maxListSize, Collection<E> l) {
        ArrayList<List<E>> res = new ArrayList<List<E>>();
        if (!isEmpty(l)) {
            int i = 0;
            List<E> subList = Lists.newArrayList(l);
            do {
                res.add(new ArrayList<E>(subList.subList(i, Math.min(i + maxListSize, l.size()))));
                i += maxListSize;
            } while (i < l.size());

        }
        return res;
    }

    /**
     * Безопасное преобразование списка в массив
     *
     * @param list   список
     * @param eClass класс элементов массива
     * @param <E>    тип элементов массива
     */
    public static <E> E[] toArray(Collection<E> list, Class<E> eClass) {
        if (list == null) {
            return null;
        }
        E[] array = Unchecked.<E[]>cast(Array.newInstance(eClass, list.size()));
        int i = 0;
        for (E el : list) {
            array[i++] = el;
        }
        return array;
    }

    /**
     * Объединяет несколько списков в один
     */
    public static <E> List<E> merge(Collection<E>... l) {
        ArrayList<E> res = new ArrayList<E>();
        if (l != null && l.length > 0) {
            for (Collection<E> collection : l) {
                if (!isEmpty(collection)) {
                    res.addAll(collection);
                }
            }
        }
        return res;
    }

    /**
     * Объединяет несколько массивов в один список
     */
    public static <E> List<E> merge(E[]... l) {
        ArrayList<E> res = new ArrayList<E>();
        if (l != null && l.length > 0) {
            for (E[] array : l) {
                if (array != null && array.length > 0) {
                    res.addAll(Arrays.asList(array));
                }
            }
        }
        return res;
    }

    /**
     * Если исходный список null то возвращает новый пустой список
     */
    public static <E> List<E> noNull(Collection<E> src) {
        if (src == null) {
            return new ArrayList<E>();
        }
        return new ArrayList<E>(src);
    }

    /**
     * Сортирует дерево, рекурсивно
     *
     * @param rootItems   верхний уровень узлов
     * @param comparator  компаратор
     * @param childFinder функция доступа к дочерним элементам узла
     */
    public static <T> void sortTree(List<T> rootItems, Comparator<T> comparator, Function<T, List<T>> childFinder) {
        if (rootItems != null) {
            rootItems.sort(comparator);
            for (T rootItem : rootItems) {
                sortTree(childFinder.apply(rootItem), comparator, childFinder);
            }
        }
    }

    /**
     * Проверка списка на наличие данных
     */
    public static boolean isEmpty(Collection list) {
        return list == null || list.isEmpty();
    }

    /**
     * Размер списка
     */
    public static int size(Collection list) {
        return list == null ? 0 : list.size();
    }

    /**
     * Проверяет наличие элемента в списке
     */
    public static <T> boolean hasElement(Collection<T> list, T element) {
        return !isEmpty(list) && element != null && list.contains(element);
    }

    /**
     * Проверяет наличие всех элементов в списке
     */
    public static <T> boolean hasAllElements(Collection<T> list, Collection<T> elements) {
        if (isEmpty(list)) return isEmpty(elements);
        if (!isEmpty(elements))
            for (T element : elements) {
                if (!list.contains(element)) return false;
            }
        return true;
    }

    /**
     * Безопасное извлечение из колекции
     */
    public static <T> T safeGet(List<T> list, int i, T defElement) {
        return size(list) <= i ? defElement : list.get(i);
    }

    public static <T> String toString(List<T> parts, String separator) {
        StringBuilder sb = new StringBuilder();
        for (T part : parts) {
            if (!StringUtil.isTrimmedEmpty(part)) {
                if (sb.length() > 0) {
                    sb.append(separator);
                }
                sb.append(part);
            }
        }
        return sb.toString();
    }

    public static <A, B> Map<B, List<A>> groupList(Collection<A> list, Function<A, B> fn) {
        Map<B, List<A>> result = Maps.newLinkedHashMap();
        for (A a : list) {
            B key = fn.apply(a);
            if (!result.containsKey(key)) {
                result.put(key, Lists.<A>newArrayList());
            }
            result.get(key).add(a);
        }
        return result;
    }

    public static <A, B> Map<B, List<A>> groupListExt(Collection<A> list, Function<A, List<B>> fn) {
        Map<B, List<A>> result = Maps.newLinkedHashMap();
        for (A a : list) {
            List<B> keys = fn.apply(a);
            if (keys != null) {
                for (B key : keys) {
                    if (!result.containsKey(key)) {
                        result.put(key, Lists.<A>newArrayList());
                    }
                    result.get(key).add(a);
                }
            }
        }
        return result;
    }

}
