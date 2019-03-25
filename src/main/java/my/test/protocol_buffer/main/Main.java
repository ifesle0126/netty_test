package my.test.protocol_buffer.main;

import com.google.protobuf.InvalidProtocolBufferException;
import my.test.protocol_buffer.data.StudentInfo;

public class Main {

    public static void main(String[] args) throws InvalidProtocolBufferException {
        StudentInfo.Student student = StudentInfo.Student.newBuilder()
                .setName("zhujq")
                .setAge(30)
                .setAddress("北京市朝阳区")
                .build();

        byte[] student_bytes = student.toByteArray();

        StudentInfo.Student student_from_bytes = StudentInfo.Student.parseFrom(student_bytes);

        System.out.println(student_from_bytes);

        System.out.println(student_from_bytes.getAddress());
    }
}
