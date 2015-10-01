
#Overview

This project is a simple multilevel cache implementation.

Cache provides base functions:

1. Put item in the cache.
2. Get item from the cache.

User may define few parameters:

1. Cache strategy.
2. Type of each cache level.
3. Size of each cache level.

List of the supported strategies:

1. LRU strategy. 
2. Random strategy.

List of the supported level types:

1. In memory (RAM).
2. In filesystem.

## Requirements

Cache can work only with class that implements interface Cacheable.

    public interface Cacheable extends Serializable {
        long getId();
    }
    
Id must be unique for the instance that need to be cached. It means that id must be unique not only for class instance, but for any instance of any class that implements Cacheable.

#Usage

## Cache creation

This is the example of getting 2-level cache, first in memory, second in file. In-memory level maximum size is 10 items, In-file level size is 100 items

    LinkedHashMap<Level, Integer> cacheLevels = new LinkedHashMap<>();
    cacheLevels.put(Level.MEMORY, 10);
    cacheLevels.put(Level.FILE, 100);
    Cache cache = cacheFactory.getCache(CacheStrategy.LEAST_RECENTLY_USED, cacheLevels);

## Store item in the cache

    Cache.put(Cacheable cacheable);
    
Item will be stored in the cache.

### Least recently used strategy (LRU)

Cache performs the following saving procedure on the top level:

1. If the level is not full - save item on the level.
2. If the level is full - replace the eldest item from this level with the one that need to be stored and:
    * if next level exists - call the saving procedure for the replaced item on next level.
    * if next level not exists - do nothing (replaced item will be lost).

### Random replacement strategy (RR)

Cache performs the following saving procedure on the top level:

1. If the level is not full - save item on the level.
2. If the level is full - replace random item from this level with the one that need to be stored and:    
    * if next level exists - call the saving procedure for the replaced item on next level.
    * if next level not exists - do nothing (replaced item will be lost).

## Get item from the cache

    Cache.get(long id);
    
Returns stored item from the cache. If there is no item with the specified id in the cache null will be returned. May result to change cached item status, depends on cache strategy.
    
### Least recently used strategy (LRU)

Requested item becomes the newest one. Cache removes the requested item from it current position and call the saving procedure for it to the top level.

### Random replacement strategy (RR)
    
Cache don't change. 
    
## Example

Declare class that need to be stored in the cache: 
    
    class CachingData implements Cacheable {
        
        private long id;
        
        public CachingData(long id) {
            this.id = id;
        }
        
        @Override long (get id) {
            return this.id;
        }
    }
    
Using the cache:

    public static void main(String args[]) {
    
    
         CacheFactory factory = CacheFactory.getInstance(); // Getting the cache factory
         LinkedHashMap<Level, Integer> cacheLevels = new LinkedHashMap<>();
         cacheLevels.put(Level.MEMORY, 10); //The first level is in memory, can keep 10 items.
         cacheLevels.put(Level.FILE, 100);  //The second level is in file system, can keep 100 items.
         Cache cache = cacheFactory.getCache(CacheStrategy.LEAST_RECENTLY_USED, cacheLevels); // Creating the cache.
         
         
         CachingData data1 = new CachingData(1);
         CachingData data2 = new CachingData(2);
         
         cache.put(data1); // new item stored, cache size is 1 now
         cache.put(data2); // new item stored, cache size is 2 now. Item with id = 1 is the eldest now.
         
         cache.put(data1); // item with the id = 1 has been replaced with the new one, cache size is still 2. Item with id = 2 is the eldest now.
         
         CachingData requested = cache.get(2); // Variable "requested" contains item with id = 2 now. Item with id = 1 is the eldest now.
         
         CachingData requested = cache.get(3); // requested contains null.
    }