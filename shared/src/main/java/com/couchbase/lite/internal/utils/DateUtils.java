//
// DateUtils.java
//
// Copyright (c) 2017 Couchbase, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.couchbase.lite.internal.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtils {
    private static SimpleDateFormat sdf;

    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private DateUtils() {
    }

    public static String toJson(Date date) {
        return sdf.format(date);
    }

    public static Date fromJson(String json) {
        if (json == null) return null;
        try {
            return sdf.parse(json);
        } catch (ParseException e) {
            return null;
        }
    }
}
