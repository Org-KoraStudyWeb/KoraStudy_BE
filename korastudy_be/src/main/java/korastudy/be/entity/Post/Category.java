package korastudy.be.entity.Post;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "category")
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "categoryId") // Chỉ định tên cột thực tế trong database
    private Long id; // Giữ tên field là id trong Java

    @Column(name = "title", columnDefinition = "NVARCHAR(255)")
    private String name; // Thay đổi từ categoryTitle thành name nhưng giữ nguyên tên cột trong DB

    @Column(name = "category_context", columnDefinition = "NVARCHAR(500)")
    private String description; // Thay đổi từ context thành description nhưng giữ nguyên tên cột trong DB

    @ManyToMany(mappedBy = "categories")
    private List<Post> posts = new ArrayList<>(); // Khởi tạo ArrayList trống để tránh NPE
}