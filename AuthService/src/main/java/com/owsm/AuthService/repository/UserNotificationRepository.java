package com.owsm.AuthService.repository;

import com.owsm.AuthService.model.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    Optional<UserNotification> findByIdAndRecipientId(Long id, Long recipientId);

    @Modifying
    @Query("UPDATE UserNotification n SET n.readAt = :readAt "
            + "WHERE n.recipient.id = :recipientId AND n.readAt IS NULL")
    int markAllReadByRecipientId(
            @Param("recipientId") Long recipientId,
            @Param("readAt") Instant readAt
    );

    @Modifying
    @Query("delete from UserNotification notification where notification.recipient.id = :recipientId")
    void deleteAllByRecipientId(@Param("recipientId") Long recipientId);
}