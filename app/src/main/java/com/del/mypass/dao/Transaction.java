package com.del.mypass.dao;

/**
 * Created by DodolinEL
 * date: 21.05.2024
 */
interface Transaction<T, R> {

    R begin(T t) throws Exception;

}
