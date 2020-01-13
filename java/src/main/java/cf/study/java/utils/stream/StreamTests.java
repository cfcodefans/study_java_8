package cf.study.java.utils.stream;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import misc.MiscUtils;

public class StreamTests {

    static class DummyList<E> extends ArrayList<E> {
        private static final long serialVersionUID = 1L;

    }

    @SuppressWarnings("unused")
    @Test
    public void findNumbersGreaterThan5() {
        Integer[] numbers = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3};
        {
            // here we keep our results in list
            List<Integer> resultList = new ArrayList<Integer>();
            for (int i = 0, j = numbers.length; i < j; i++) {
                if (numbers[i] > 5) {
                    resultList.add(numbers[i]);
                }
            }
        }

        {
            List<Integer> asList = Arrays.asList(numbers);
            Predicate<Integer> greaterThan5 = new Predicate<Integer>() {
                public boolean apply(Integer input) {
                    return input > 5;
                }
            };
            Collection<Integer> results = Collections2.filter(asList, greaterThan5);
        }

        {
            List<Integer> resultList = new ArrayList<Integer>();
            Consumer<Integer> consumer = new Consumer<Integer>() {
                public void accept(Integer number) {
                    if (number > 5) {
                        resultList.add(5);
                    }
                }
            };
            Stream.of(numbers).forEach(consumer);
        }

        {
            final List<Integer> resultList = new ArrayList<Integer>();
            Stream.of(numbers).forEach((Integer number) -> {
                if (number > 5) {
                    resultList.add(number);
                }
            });
        }

        {
            List<Integer> resultList = Stream.of(numbers).filter(number -> number > 5).collect(Collectors.toList());
            Consumer<Integer> consumer = new Consumer<Integer>() {
                public void accept(Integer number) {
                    resultList.add(number);
                }
            };
        }
    }

    @Test
    public void example() {

        {// find out sum of all the integers greater than 5.
            final List<Integer> intList = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
            // Collections.addAll(new ArrayList<Integer>(), 3, 1, 4, 1, 5, 9, 2,
            // 6, 5, 3, 5);

            // Prior to Java 8, it could be:
            int sum1 = 0;
            for (final Integer iv : intList) {
                if (iv > 5)
                    sum1 += iv;
            }

            int sum2 = intList.stream().filter(i -> (i > 5)).mapToInt(i -> i).sum();

            Assert.assertEquals(sum1, sum2);
        }
    }

    @Test
    @SuppressWarnings("unused")
    public void createStream() {
        {
            Stream<Integer> intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
            intStream.forEach(i -> {
                System.out.print(i + ", ");
            });
            System.out.println();
        }

        {
            Stream<Integer> intStream = Stream.of(new Integer[]{3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5});
            intStream.forEach(i -> {
                System.out.print(i + ", ");
            });
            System.out.println();

            {
                Stream<Integer> parallelIntStream = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5).parallelStream();
                parallelIntStream.forEach(i -> {
                    System.out.print(i + ", ");
                });
                System.out.println();
            }
        }

        if (false) {
            Stream<String> strStream1 = Stream.generate(new Supplier<String>() {
                int i = 0;

                public String get() {
                    i++;
                    if (i > 10)
                        return null;
                    return "from a Supplier";
                }
            });

            strStream1.forEach(i -> {
                System.out.println(i);
            });
        }

        {
            String str = "abcdefg";
            str.chars().forEach(c -> {
                System.out.println((char) c);
            });
        }

        {
            String str1 = "abcdefg";
            String str2 = "abcdefg";

            IntStream stream1 = str1.chars();
            IntStream stream2 = str2.chars();

            IntStream concated = IntStream.concat(stream1, stream2);

            concated.forEach(c -> {
                System.out.print((char) c + ", ");
            });
        }
    }

    @Test
    public void toCollectionAndArray() {
        Stream<Integer> intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        List<Integer> intList = intStream.collect(Collectors.toList());
        System.out.println(intList);

        intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        Map<?, ?> intAndChars = intStream.distinct().collect(Collectors.toMap(i -> i, i -> (char) (97 + i)));
        System.out.println(intAndChars);

        intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        Integer[] intArray = intStream.toArray(Integer[]::new);
        System.out.println(Arrays.toString(intArray));
    }

    @Test
    public void filter() {
        Stream<Integer> intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        int sum = intStream.filter(i -> (i % 2 == 0)).collect(Collectors.summingInt(i -> i));
        System.out.println(sum);

        sum = 0;
        for (final int i : new int[]{3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5}) {
            if (i % 2 == 0)
                sum += i;
        }
        System.out.println(sum);

        intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        intStream.filter((i) -> {
            System.out.println(i);
            return i == 1;
        }).findFirst();
    }

    @Test
    public void map() {
        Stream<Integer> intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        intStream.map(i -> {
            return Math.abs(10 - i);
        }).forEach(i -> {
            System.out.print(i + ", ");
        });

        intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        Object[] array = intStream.map(i -> {
            return String.valueOf(i);
        }).toArray();
        Stream.of(array).forEach(obj -> {
            System.out.println(obj.getClass());
        });
    }

    @Test
    public void sorted() {
        Stream<Integer> intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        List<Integer> intList = intStream.sorted().collect(Collectors.toList());
        System.out.println(intList);
    }

    @Test
    public void flatMap() {
        Stream<List<String>> listStream = Stream.of(Arrays.asList("html", "javascript", "css"), Arrays.asList("servlet", "filter", "jsp"), Arrays.asList("ejb", "jms", "jpa"));

        Stream<String> flatStream = listStream.flatMap(strList -> strList.stream());
        flatStream.forEach(System.out::println);
    }

    @Test
    public void foldLeft() {

    }

    @Test
    public void reduce() {
        {
            final IntBinaryOperator op = (_sum, i) -> (_sum += i);
            System.out.println(IntStream.range(1, 10).reduce(op).getAsInt());
        }

        {
            Stream<Integer> intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
            Optional<Integer> sum = intStream.reduce((_sum, i) -> (_sum += i));
            System.out.println(sum.get());
        }

        {
            final List<BigDecimal> prices = Arrays.asList(new BigDecimal("10"), new BigDecimal("30"), new BigDecimal("17"), new BigDecimal("20"), new BigDecimal("15"), new BigDecimal("18"), new BigDecimal("45"), new BigDecimal("12"));

            BigDecimal totalOfDiscountedPrices = prices.stream().filter(price -> price.compareTo(BigDecimal.valueOf(20)) > 0).map(price -> price.multiply(BigDecimal.valueOf(0.9))).reduce(BigDecimal.ZERO, BigDecimal::add);
            System.out.println("Total of discounted prices: " + totalOfDiscountedPrices);

            totalOfDiscountedPrices = BigDecimal.ZERO;
            for (BigDecimal price : prices) {
                if (price.compareTo(BigDecimal.valueOf(20)) > 0)
                    totalOfDiscountedPrices = totalOfDiscountedPrices.add(price.multiply(BigDecimal.valueOf(0.9)));
            }
            System.out.println("Total of discounted prices: " + totalOfDiscountedPrices);
        }
    }

    @Test
    public void count() {
        Stream<Integer> intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        System.out.println(intStream.count());
    }

    @Test
    public void match() {
        Stream<Integer> intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);

        System.out.println("even numbers: " + intStream.anyMatch(i -> (i % 2 == 0)));

        intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        System.out.println("even numbers: " + intStream.allMatch(i -> (i % 2 == 0)));

        intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        System.out.println("odd numbers: " + intStream.anyMatch(i -> (i % 2 == 1)));

        intStream = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        System.out.println("odd numbers: " + intStream.noneMatch(i -> (i % 2 == 0)));
    }

    static class Record {
        static int seq = 0;

        Record(int _value) {
            value = _value;
            seq++;
        }

        Record() {
            seq++;
            System.out.println(MiscUtils.stackInfo() + "\n");
        }

        public int value;

        @Override
        public String toString() {
            return super.toString() + "= {value: " + value + "}";
        }
    }

    @Test
    public void testCollector() {
        List<Long> longs = MiscUtils.pi2Longs(100);
        List<Long> collected = longs.stream().filter(n -> n.intValue() % 2 == 0).collect(Collectors.toList());
        System.out.println(collected);

        StringBuilder collectedSB = longs.stream().filter(n -> n.intValue() % 2 != 0).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);
        System.out.println(collectedSB);


        longs = Arrays.asList(1l, 2l, 3l, 4l);
        Long[] sumRef = {0l};

        BiConsumer<Long[], Long> accumulator = (Long[] s, Long e) -> {
            System.out.println(MiscUtils.invocationInfo() + String.format(" accumulator.accept(%d, %d)", s[0], e));
            s[0] = s[0] + e;
        };
        BiConsumer<Long[], Long[]> combiner = (Long[] s, Long[] e) -> {
            System.out.println(MiscUtils.invocationInfo() + String.format(" combiner.accept(%d, %d)", s[0], e[0]));
            s[0] = s[0] + e[0];
        };
        Supplier<Long[]> supplier = () -> sumRef;

        longs.stream().map(Long::valueOf).collect(supplier, accumulator, combiner);
        System.out.println(sumRef[0]);

        sumRef[0] = 0l;
        longs.stream().map(Long::valueOf).parallel().collect(supplier, accumulator, combiner);
        System.out.println(sumRef[0]);
    }

    @Test
    public void testInnerMechanism() {
        {
            final Record record1 = new Record(1);
            final Record record2 = new Record(2);
            final Record record3 = new Record(3);

            Stream<Record> records = Stream.of(record1, record2, record3);
            final Optional<Record> reduced = records.reduce((_record, i) -> {
                _record.value += i.value;
                return _record;
            });
            System.out.println(reduced.get().value);
            System.out.println(Record.seq);

            System.out.println(record1);
            System.out.println(record2);
            System.out.println(record3);
        }

        {
            final Record record1 = new Record(1);
            final Record record2 = new Record(2);
            final Record record3 = new Record(3);
            final Record recordSum = new Record();

            Stream<Record> records = Stream.of(record1, record2, record3);
            final Record reduced = records.reduce(recordSum, (_record, i) -> {
                _record.value += i.value;
                return _record;
            });
            System.out.println(reduced.value);
            System.out.println(Record.seq);

            System.out.println(record1);
            System.out.println(record2);
            System.out.println(record3);
            System.out.println(recordSum);
        }
    }

    @Test
    public void testSupplier() {
        LongStream ls = LongStream.generate(System::currentTimeMillis);
        System.out.println(ls.findFirst().getAsLong());

        Stream<Calendar> cs = Stream.generate(Calendar::getInstance);
        System.out.println(cs.findFirst().get());

        Integer[] intArray = {3, 1, 4, 1, 5, 9};
        List<Integer> intList = Arrays.asList(intArray);
        final Iterator<Integer> it = intList.iterator();
        Supplier<Integer> s = () -> {
            return it.hasNext() ? it.next() : null;
        };
        Stream.generate(s).forEach(System.out::println);

    }

    @Test
    public void testRange() {
        IntStream.range(0, 10).forEach(System.out::println);
    }

    interface Foo {
        void bar();
    }

    Foo foo = () -> System.out.println("Foo.bar");

    private void callFoo(Foo f) {
        f.bar();
    }

    @Test
    public void testLambda() {
        callFoo(() -> System.out.println("Foo.bar"));
        callFoo(System.out::println);
    }

    @Test
    public void testConcat() {
        Stream.concat(Stream.of("Hello".toCharArray()), Stream.of("World".toCharArray())).forEach(System.out::println);
    }

    @Test
    public void testDistinct() {
        List<Long> longs = MiscUtils.pi2Longs(20);
        System.out.println(longs);
        System.out.println(longs.stream().distinct().collect(Collectors.toList()));
    }

    @Test
    public void testPeek() {
        List<Long> longs = MiscUtils.pi2Longs(20);
        longs.stream().peek(e -> System.out.printf("fetching:\t%s\n", e)).limit(5).forEach(e -> System.out.printf("\tget:\t%s\n", e));
    }
}