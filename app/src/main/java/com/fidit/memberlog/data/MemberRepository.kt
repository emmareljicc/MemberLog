package com.fidit.memberlog.data

import com.fidit.memberlog.model.Member
import kotlinx.coroutines.flow.Flow

class MemberRepository(private val dao: MemberDao) {

    val allMembers: Flow<List<Member>> = dao.getAll()

    suspend fun insert(member: Member): Long = dao.insert(member)

    suspend fun update(member: Member) = dao.update(member)

    suspend fun delete(member: Member) = dao.delete(member)
}
