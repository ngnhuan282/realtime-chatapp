package com.example.ChatServer.repository;

import com.example.ChatServer.entity.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<ChatGroup, Integer> {

    /**
     * Lấy tất cả group mà user tham gia (làm creator hoặc member)
     */
    @Query("SELECT DISTINCT g FROM ChatGroup g " +
            "LEFT JOIN GroupMember gm ON gm.group.id = g.id " +
            "WHERE g.creator.id = :userId OR gm.user.id = :userId")
    List<ChatGroup> findMyGroups(@Param("userId") Integer userId);
}