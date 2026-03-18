import com.example.ChatServer.entity.ChatGroup;
import com.example.ChatServer.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "senderId")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiverId")
    private User receiver; // Null nếu là chat nhóm

    @ManyToOne
    @JoinColumn(name = "groupId")
    private ChatGroup group; // Null nếu là chat 1-1

    private String content;
    private String messageType; // TEXT, IMAGE, VIDEO, etc.
    private String status;      // SENT, DELIVERED, READ

    @Column(insertable = false, updatable = false)
    private LocalDateTime createdAt;
}