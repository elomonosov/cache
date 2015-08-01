package ru.elomonosov;

public class Main {

    public static void main(String[] args) {

        /*try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("11.tmp"))) {
            Map<Long, Cacheable> map = new TreeMap<>();
            map.put(1L, new Cacheable() {
                @Override
                public long getId() {
                    return 1;
                }
            });
            map.put(2L, new Cacheable() {
                @Override
                public long getId() {
                    return 2;
                }
            });
            map.put(3L, new Cacheable() {
                @Override
                public long getId() {
                    return 3;
                }
            });
            map.put(4L, new Cacheable() {
                @Override
                public long getId() {
                    return 4;
                }
            });

            out.writeObject(map);

        } catch (IOException e) {

        }

        Map<Long, Cacheable> readMap = null;
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream("11.tmp"))) {
            readMap = (TreeMap) in.readObject();
        } catch (IOException | ClassNotFoundException e) {

        }

        for (Map.Entry<Long, Cacheable> entry : readMap.entrySet()) {
            System.out.println(entry.getValue().getId());
        }*/
    }
}
