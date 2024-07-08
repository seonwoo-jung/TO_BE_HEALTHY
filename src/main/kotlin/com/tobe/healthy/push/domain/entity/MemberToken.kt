package com.tobe.healthy.push.domain.entity

import com.tobe.healthy.common.BaseTimeEntity
import com.tobe.healthy.member.domain.entity.Member
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.GenerationType.IDENTITY
import lombok.ToString

@Entity
class MemberToken(

    var deviceType: String,

    var token: String,

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    @ToString.Exclude
    val member: Member? = null,

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "member_token_id")
    val id: Long? = null

) : BaseTimeEntity<MemberToken, Long>() {

    fun changeToken(token: String) {
        this.token = token
    }

    companion object {
        fun register(member: Member, token: String, deviceType: String): MemberToken {
            return MemberToken(
                member = member,
                token = token,
                deviceType = deviceType
            )
        }
    }
}