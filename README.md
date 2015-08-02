
#Overview

This project is a simple multilevel cache implementation.

Cache provides base functions:

1. Put item in the cache.
2. Get item from the cache.

User may declare some parameters:

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
    
Id must be unique for the instance that need to be cached. It means that id must be unique not only for class instance, but any instance of any classes that are Cacheable.

#Usage

## Cache creation

    CacheFactory factory = CacheFactory.getInstance();
    factory.addLevel(Level.MEMORY, 10);
    factory.addLevel(Level.FILE, 100);
    cacheFactory.setCacheStrategy(CacheStrategy.LEAST_RECENTLY_USED);
    Cache cache = factory.getCache();
    
This is the example of getting 2-level cache, first in memory, second in file. In-memory level maximum size is 10 items, In-file level size is 100 items.

## Store item in the cache

    Cache.put(Cacheable cacheable);
    
Item will be stored in the cache, positioned on the top level.

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

Requested item becomes the newest one. Cache removes the requested item from it current position where it was stored and call the saving procedure for it to the first level.

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
         factory.addLevel(Level.MEMORY, 10); // Top level is in memory, can keep 10 items.
         factory.addLevel(Level.FILE, 100); // Second level is in file, can keep 100 items.
         cacheFactory.setCacheStrategy(CacheStrategy.LEAST_RECENTLY_USED); // Set the cache strategy to "Least Recently Used strategy"
         Cache cache = factory.getCache(); // Creating the cache.
         
         CachingData data1 = new CachingData(1);
         CachingData data2 = new CachingData(2);
         
         cache.put(data1); // new item stored, cache size is 1 now
         cache.put(data2); // new item stored, cache size is 2 now. Item with id = 1 is the eldest now.
         
         cache.put(data1); // item with the id = 1 has been replaced with the new one, cache size is still 2. Item with id = 2 is the eldest now.
         
         CachingData requested = cache.get(2); // requested contains item with id = 2 now. In cache, item with id = 1 is the eldest now.
         
         CachingData requested = cache.get(3); // requested contains null.
    }

#API Reference

## Cache

## Cache level

