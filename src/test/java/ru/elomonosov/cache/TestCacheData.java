package ru.elomonosov.cache;

public class TestCacheData implements Cacheable {
    private long id;

    private String object = null;

    public TestCacheData(long id, String object) {
        this.id = id;
        this.object = object;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestCacheData)) return false;

        TestCacheData that = (TestCacheData) o;

        if (id != that.id) return false;
        return !(object != null ? !object.equals(that.object) : that.object != null);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (object != null ? object.hashCode() : 0);
        return result;
    }
}
