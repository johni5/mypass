package com.del.mypass.dao;

import javax.persistence.EntityManager;

public interface EntityManagerProvider {

    EntityManager getEntityManager();

}
