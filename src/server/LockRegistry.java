package server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Fabio Somaglia on 02/01/18.
 */

public enum LockRegistry {

	INSTANCE; //rendiamo la classe un singleton

	private Map<String, ReadWriteLock> lockMap = new HashMap<>(); //HashMap che contiene l'abbinamento nome del file e lock

	private Lock registryLock = new ReentrantLock(); //lock utilizzato per evitare ai diversi thread di istanziare lock con le stesse chiavi

	//specifica del lock
	public enum LockType {
		READ, WRITE
	}

	public void acquire(String fileName, LockType type) {

		ReadWriteLock lock = retrieveLock(fileName);

		switch (type) {
			case READ:
				lock.readLock().lock();
				break;
			case WRITE:
				lock.writeLock().lock();
				break;
			default:
				break;
		}

	}

	public void release(String fileName, LockType type) {

		ReadWriteLock lock = retrieveLock(fileName);

		switch (type) {

			case READ:
				lock.readLock().unlock();
				break;
			case WRITE:
				lock.writeLock().unlock();
				break;
			default:
				break;
		}

	}

	private ReadWriteLock retrieveLock(String fileName) {

		ReadWriteLock newLock = null;

		try {
			registryLock.lock();

			newLock = lockMap.get(fileName);

			//crea un lock e lo aggiunge all'HashMap, se non esiste già
			if (newLock == null) {
				newLock = new ReentrantReadWriteLock();
				lockMap.put(fileName, newLock);
			}
		} finally {
			registryLock.unlock();
		}

		return newLock;

	}

}