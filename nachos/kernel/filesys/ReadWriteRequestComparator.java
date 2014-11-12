package nachos.kernel.filesys;

import java.util.Comparator;

public class ReadWriteRequestComparator implements Comparator<ReadWriteRequest> {

    @Override
    public int compare(ReadWriteRequest o1, ReadWriteRequest o2) {
        // write comparison logic here like below , it's just a sample
        return o1.compareTo(o2);
    }
}
