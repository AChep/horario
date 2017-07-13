/*
 * Copyright (C) 2017 XJSHQ@github.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.artemchep.horario.database.navigation;

import com.artemchep.horario.database.models.SubjectInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Artem Chepurnoy
 */
public class Dbb {

    public static final String USERS = "_users_";
    public static final String USER_TIMETABLES = "timetables";
    public static final String USER_TIMETABLE_SUBJECTS = "subjects";
    public static final String USER_TIMETABLE_ABSENCES = "absences";
    public static final String USER_TIMETABLE_NOTES = "notes";
    public static final String SUBJECTS = "_subjects_";
    public static final String SUBJECT_GROUPS = "groups";
    public static final String SUBJECT_GROUP_USERS = "users";
    public static final String SUBJECT_GROUP_LESSONS = "lessons";
    public static final String SUBJECT_GROUP_TASK = "tasks";
    public static final String SUBJECT_GROUP_TASK_COMMENTS = "comments";
    public static final String SUBJECT_GROUP_TASK_RESULT = "result";
    public static final String SUBJECT_GROUP_TASK_INFO = "info";
    public static final String SUBJECT_GROUP_INFO = "info";
    public static final String SUBJECT_EDITORS = "editors";
    public static final String SUBJECT_INFO = "info";

    public static void subjectPush(SubjectInfo subjectInfo, String userId, String timetableId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        // Init subject with an owner id
        DatabaseReference subjectRef = ref.child(SUBJECTS).push();
        subjectRef.child("creator").setValue(userId);
        subjectInfo.key = subjectRef.getKey();

        // Fulfil the subject
        Map<String, Object> map = new HashMap<>();
        map.put(SUBJECT_INFO, subjectInfo);
        map.put(SUBJECT_EDITORS + "/" + userId, 1); // value doesn't matter
        subjectRef.updateChildren(map);

        // Add this subject to user; this subject is basically a copy-paste
        // from original one
        SubjectInfo clone = subjectInfo.clone();
        clone.info = null;
        ref
                .child(USERS).child(userId)
                .child(USER_TIMETABLES).child(timetableId)
                .child(USER_TIMETABLE_SUBJECTS).child(subjectInfo.key).setValue(clone);
    }

    public static void subjectUpdate(SubjectInfo subjectInfo) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(SUBJECTS)
                .child(subjectInfo.key)
                .child(SUBJECT_INFO)
                .setValue(subjectInfo);
    }

    public static void subjectUpdateLocal(SubjectInfo subjectInfo, String userId, String timetableId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        SubjectInfo clone = subjectInfo.clone();
        clone.info = null;
        ref
                .child(USERS).child(userId)
                .child(USER_TIMETABLES).child(timetableId)
                .child(USER_TIMETABLE_SUBJECTS).child(subjectInfo.key).setValue(clone);
    }

}
