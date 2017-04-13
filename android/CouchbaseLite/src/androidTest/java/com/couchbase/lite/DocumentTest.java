/**
 * Copyright (c) 2017 Couchbase, Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.couchbase.lite;

import com.couchbase.litecore.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DocumentTest extends BaseTest {
    private static final String TAG = DocumentTest.class.getName();


    @Before
    public void setUp() {
        super.setUp();
        // TODO: DB005 - ConflictResolver
    }

    @After
    public void tearDown() {
        doc.revert();
        super.tearDown();
    }

    @Test
    public void testNewDoc() {
        Document doc = db.getDocument();
        assertNotNull(doc);
        assertNotNull(doc.getID());
        assertTrue(doc.getID().length() > 0);
        assertEquals(db, doc.getDatabase());
        assertFalse(doc.exists());
        assertFalse(doc.isDeleted());
        assertNull(doc.getProperties());
        assertNull(doc.get("prop"));
        assertFalse(doc.getBoolean("prop"));
        assertEquals(0, doc.getInt("prop"));
        assertEquals(0.0, doc.getDouble("prop"), 0.0);
        assertNull(doc.getDate("prop"));
        assertNull(doc.getString("prop"));

        doc.save();
        assertTrue(doc.exists());
        assertFalse(doc.isDeleted());
        assertNull(doc.getProperties());
    }

    @Test
    public void testNewDocWithId() {
        Document doc = db.getDocument("doc1");
        assertNotNull(doc);
        assertNotNull(doc.getID());
        assertEquals("doc1", doc.getID());
        assertEquals(db, doc.getDatabase());
        assertFalse(doc.exists());
        assertFalse(doc.isDeleted());
        assertNull(doc.getProperties());
        assertNull(doc.get("prop"));
        assertFalse(doc.getBoolean("prop"));
        assertEquals(0, doc.getInt("prop"));
        assertEquals(0.0, doc.getDouble("prop"), 0.0);
        assertNull(doc.getDate("prop"));
        assertNull(doc.getString("prop"));

        doc.save();
        assertTrue(doc.exists());
        assertFalse(doc.isDeleted());
        assertNull(doc.getProperties());
        assertEquals(doc, db.getDocument("doc1"));
    }

    @Test
    public void testIterator() {
        Document doc = db.getDocument("doc1");

        // Primitives:
        doc.set("bool", true);
        doc.set("double", 1.1);
        doc.set("integer", 2);

        // iterator
        Iterator<String> itr = doc.iterator();
        assertNotNull(itr);
        int i = 0;
        while (itr.hasNext()) {
            String key = itr.next();
            i++;
        }
        assertEquals(3, i);

        ////// Save Document
        doc.save();

        // iterator
        itr = doc.iterator();
        assertNotNull(itr);
        i = 0;
        while (itr.hasNext()) {
            String key = itr.next();
            i++;
        }
        assertEquals(3, i);

        ////// Reopen the database and get the document again:
        reopenDB();

        Document doc1 = db.getDocument("doc1");
        assertNotNull(doc1);

        // iterator
        Iterator<String> itr1 = doc1.iterator();
        assertNotNull(itr1);
        i = 0;
        while (itr1.hasNext()) {
            String key = itr1.next();
            i++;
        }
        assertEquals(3, i);
    }

    @Test
    public void testPropertyAccessors() {
        Document doc = db.getDocument("doc1");

        // Primitives:
        doc.set("bool", true);
        doc.set("double", 1.1);
        doc.set("integer", 2);

        // Objects:
        doc.set("string", "str");
        doc.set("boolObj", Boolean.TRUE);
        doc.set("number", Integer.valueOf(1));
        Map<String, String> dict = new HashMap<>();
        dict.put("foo", "bar");
        doc.set("dict", dict);
        List<String> list = Arrays.asList("1", "2");
        doc.set("array", list);

        // null
        doc.set(null, null);
        doc.set("nullarray", Arrays.asList(null, null));

        // Date:
        Date date = new Date();
        doc.set("date", date);

        // save doc
        doc.save();

        // Primitives:
        assertEquals(true, doc.getBoolean("bool"));
        assertEquals(1.1, doc.getDouble("double"), 0.0);
        assertEquals(2, doc.getInt("integer"));

        // Objects:
        assertEquals("str", doc.get("string"));
        assertEquals(Boolean.TRUE, doc.get("boolObj"));
        assertEquals(Integer.valueOf(1), doc.get("number"));
        assertEquals(dict, doc.get("dict"));
        assertEquals(list, doc.get("array"));

        // null
        assertEquals(null, doc.get(null));
        assertEquals(Arrays.asList(null, null), doc.get("nullarray"));

        // Date: once serialized, truncate less than a second
        assertTrue(Math.abs(date.getTime() - doc.getDate("date").getTime()) < 1000);

        ////// Reopen the database and get the document again:
        reopenDB();

        Document doc1 = db.getDocument("doc1");
        assertNotNull(doc1);

        // Primitives:
        assertEquals(true, doc1.getBoolean("bool"));
        assertEquals(1.1, doc1.getDouble("double"), 0.0);
        assertEquals(2, doc1.getInt("integer"));

        // Objects:
        assertEquals("str", doc1.getString("string"));
        assertEquals("str", doc1.get("string"));
        assertEquals(Boolean.TRUE, doc1.get("boolObj"));
        assertEquals(Integer.valueOf(1), doc1.get("number"));
        assertEquals(dict, doc1.get("dict"));
        assertEquals(list, doc1.get("array"));

        // null
        assertEquals(null, doc1.get(null));
        assertEquals(Arrays.asList(null, null), doc1.get("nullarray"));

        // Date: once serialized, truncate less than a second
        assertTrue(Math.abs(date.getTime() - doc.getDate("date").getTime()) < 1000);
    }

    @Test
    public void testProperties() {
        Document doc = db.getDocument("doc1");
        doc.set("type", "demo");
        doc.set("weight", 12.5);
        doc.set("tags", Arrays.asList("useless", "temporary"));

        assertEquals("demo", doc.get("type"));
        assertEquals(12.5, doc.getDouble("weight"), 0.0000001);
        assertEquals(Arrays.asList("useless", "temporary"), doc.getArray("tags"));
        Map<String, Object> expect = new HashMap<>();
        expect.put("type", "demo");
        expect.put("weight", 12.5);
        expect.put("tags", Arrays.asList("useless", "temporary"));
        assertEquals(expect, doc.getProperties());
    }

    @Test
    public void testRemoveProperties() {
        Document doc = db.getDocument("doc1");
        Map<String, Object> props = new HashMap<>();
        props.put("type", "profile");
        props.put("name", "Jason");
        props.put("weight", 130.5);
        Map<String, Object> addresss = new HashMap<>();
        addresss.put("street", "1 milky way.");
        addresss.put("city", "galaxy city");
        addresss.put("zip", 12345);
        props.put("address", addresss);
        doc.setProperties(props);

        assertEquals(130.5, doc.getDouble("weight"), 0.0);
        assertEquals("galaxy city", ((Map<String, Object>) doc.get("address")).get("city"));

        doc.set("name", null);
        doc.set("weight", null);

        Map<String, Object> addressCopy = new HashMap<>((Map<String, Object>) doc.get("address"));
        addressCopy.put("city", null);
        doc.set("address", addressCopy);

        assertNull(doc.get("name"));
        assertNull(doc.get("weight"));
        assertEquals(0.0, doc.getDouble("weight"), 0.0);
        assertNull(((Map<String, Object>) doc.get("address")).get("city"));
    }

    @Test
    public void testContainsKey() {
        Document doc = db.getDocument("doc1");
        Map<String, Object> props = new HashMap<>();
        props.put("type", "profile");
        props.put("name", "Jason");
        Map<String, Object> addresss = new HashMap<>();
        addresss.put("street", "1 milky way.");
        props.put("address", addresss);
        doc.setProperties(props);

        assertTrue(doc.contains("type"));
        assertTrue(doc.contains("name"));
        assertTrue(doc.contains("address"));
        assertFalse(doc.contains("weight"));
    }

    @Test
    public void testDelete() {
        Document doc = db.getDocument("doc1");
        doc.set("type", "profile");
        doc.set("name", "Scott");
        assertFalse(doc.exists());
        assertFalse(doc.isDeleted());

        // Delete before save:
        try {
            doc.delete();
            fail("CouchbaseLiteException expected");
        } catch (CouchbaseLiteException e) {
            // should be com here...
            assertEquals(Constants.C4ErrorDomain.LiteCoreDomain, e.getDomain());
        }
        assertEquals("profile", doc.get("type"));
        assertEquals("Scott", doc.get("name"));

        // Save:
        doc.save();
        assertTrue(doc.exists());
        assertFalse(doc.isDeleted());

        // Delete:
        doc.delete();
        assertTrue(doc.exists());
        assertTrue(doc.isDeleted());
        assertNull(doc.getProperties());
    }

    @Test
    public void testPurge() {
        Document doc = db.getDocument("doc1");
        doc.set("type", "profile");
        doc.set("name", "Scott");
        assertFalse(doc.exists());
        assertFalse(doc.isDeleted());

        // Delete before save:
        try {
            doc.purge();
            fail("CouchbaseLiteException expected");
        } catch (CouchbaseLiteException e) {
            // should be com here...
        }
        assertEquals("profile", doc.get("type"));
        assertEquals("Scott", doc.get("name"));

        // Save:
        doc.save();
        assertTrue(doc.exists());
        assertFalse(doc.isDeleted());

        // Purge:
        doc.purge();
        assertFalse(doc.exists());
        assertFalse(doc.isDeleted());
    }

    @Test
    public void testRevert() {
        Document doc = db.getDocument("doc1");
        doc.set("type", "profile");
        doc.set("name", "Scott");

        // Revert before save:
        doc.revert();
        assertNull(doc.get("type"));
        assertNull(doc.get("name"));

        // Save:
        doc.set("type", "profile");
        doc.set("name", "Scott");
        doc.save();
        assertEquals("profile", doc.get("type"));
        assertEquals("Scott", doc.get("name"));

        // Make some changes:
        doc.set("type", "user");
        doc.set("name", "Scottie");

        // Revert:
        doc.revert();
        assertEquals("profile", doc.get("type"));
        assertEquals("Scott", doc.get("name"));
    }

    @Test
    public void testReopenDB() {
        Document doc = db.getDocument("doc1");
        doc.set("string", "str");
        Map<String, Object> expect = new HashMap<>();
        expect.put("string", "str");
        assertEquals(expect, doc.getProperties());
        doc.save();

        reopenDB();

        doc = db.getDocument("doc1");
        assertEquals(expect, doc.getProperties());
        assertEquals("str", doc.get("string"));
    }

    @Test
    public void testConflict() {
        // TODO: DB005
    }

    @Test
    public void testConflictResolverGivesUp() {
        // TODO: DB005
    }

    @Test
    public void testDeletionConflict() {
        // TODO: DB005
    }

    @Test
    public void testConflictMineIsDeeper() {
        // TODO: DB005
    }

    @Test
    public void testConflictTheirsIsDeeper() {
        // TODO: DB005
    }

    @Test
    public void testBlob() throws CouchbaseLiteException, IOException {
        byte[] content = "12345".getBytes();

        // store blob
        {

            Blob data = new Blob("text/plain", content);
            assertNotNull(data);
            doc.set("name", "Jim");
            doc.set("data", data);
            doc.save();

            assertEquals("Jim", doc.get("name"));
            assertTrue(doc.get("data") instanceof Blob);
            data = (Blob) doc.get("data");
            assertEquals(5, data.length());
            assertTrue(Arrays.equals(content, data.getContent()));

            closeDB();
        }

        // obtain blob
        {
            openDB();
            Document doc1 = db.getDocument("doc1");
            assertEquals("Jim", doc1.get("name"));
            assertTrue(doc1.get("data") instanceof Blob);
            Blob data = (Blob) doc1.get("data");
            assertEquals(5, data.length());
            assertTrue(Arrays.equals(content, data.getContent()));
            InputStream is = data.getContentStream();
            try {
                assertNotNull(is);
                byte[] buffer = new byte[10];
                int bytesRead = is.read(buffer);
                assertEquals(5, bytesRead);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    fail();
                }
            }
        }
    }

    @Test
    public void testEmptyBlob() throws IOException {
        Document doc = db.getDocument("doc1");
        byte[] content = "".getBytes();
        Blob data = new Blob("text/plain", content);
        assertNotNull(data);
        doc.set("data", data);
        doc.save();

        Database copyOfDB = db.copy();
        try {
            Document doc1 = copyOfDB.getDocument("doc1");
            assertTrue(doc1.get("data") instanceof Blob);
            data = (Blob) doc1.get("data");
            assertEquals(0, data.length());
            assertTrue(Arrays.equals(content, data.getContent()));
            InputStream is = data.getContentStream();
            try {
                assertNotNull(is);
                byte[] buffer = new byte[10];
                int bytesRead = is.read(buffer);
                assertEquals(0, bytesRead);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    fail();
                }
            }
        } finally {
            copyOfDB.close();
        }
    }

    @Test
    public void testBlobWithStream() throws IOException {
        byte[] content = "".getBytes();
        InputStream stream = new ByteArrayInputStream(content);
        try {
            Blob data = new Blob("text/plain", stream);
            assertNotNull(data);
            doc.set("data", data);
            doc.save();
        } finally {
            stream.close();
        }

        Database copy = db.copy();
        try {
            Document doc1 = db.getDocument("doc1");
            assertTrue(doc1.get("data") instanceof Blob);
            Blob data = (Blob) doc1.get("data");
            assertEquals(0, data.length());
            assertTrue(Arrays.equals(content, data.getContent()));
            InputStream is = data.getContentStream();
            try {
                assertNotNull(is);
                byte[] buffer = new byte[10];
                int bytesRead = is.read(buffer);
                assertEquals(0, bytesRead);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    fail();
                }
            }
        } finally {
            copy.close();
        }
    }

    @Test
    public void testMultipleBlobRead() throws IOException {
        byte[] content = "12345".getBytes();

        Blob data = new Blob("text/plain", content);
        assertNotNull(data);
        doc.set("data", data);

        data = (Blob) doc.get("data");
        for (int i = 0; i < 5; i++) {
            assertTrue(Arrays.equals(content, data.getContent()));
            InputStream is = data.getContentStream();
            try {
                assertNotNull(is);
                byte[] buffer = new byte[10];
                int bytesRead = is.read(buffer);
                assertEquals(5, bytesRead);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    fail();
                }
            }
        }

        doc.save();

        Database copy = db.copy();
        try {
            Document doc1 = db.getDocument("doc1");
            assertTrue(doc1.get("data") instanceof Blob);
            data = (Blob) doc1.get("data");
            for (int i = 0; i < 5; i++) {
                assertTrue(Arrays.equals(content, data.getContent()));
                InputStream is = data.getContentStream();
                try {
                    assertNotNull(is);
                    byte[] buffer = new byte[10];
                    int bytesRead = is.read(buffer);
                    assertEquals(5, bytesRead);
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        fail();
                    }
                }
            }
        } finally {
            copy.close();
        }
    }

    @Test
    public void testReadExistingBlob() {
        byte[] content = "12345".getBytes();

        Blob data = new Blob("text/plain", content);
        assertNotNull(data);
        doc.set("data", data);
        doc.set("name", "Jim");
        doc.save();

        assertTrue(doc.get("data") instanceof Blob);

        reopenDB();

        assertTrue(doc.get("data") instanceof Blob);
        data = (Blob) doc.get("data");
        assertTrue(Arrays.equals(content, data.getContent()));

        reopenDB();

        doc.set("foo", "bar");
        doc.save();

        assertTrue(doc.get("data") instanceof Blob);
        data = (Blob) doc.get("data");
        assertTrue(Arrays.equals(content, data.getContent()));
    }

    @Test
    public void testBlobInNestedMap() {
        byte[] content = "12345".getBytes();

        Blob data = new Blob("text/plain", content);
        Map<String, Object> map = new HashMap<>();
        map.put("data", data);
        doc.set("map", map);

        assertTrue(map.get("data") instanceof Blob);
        data = (Blob) map.get("data");
        assertTrue(Arrays.equals(content, data.getContent()));

        doc.save();

        map = (Map<String, Object>) doc.get("map");
        assertNotNull(map);
        assertTrue(map.get("data") instanceof Blob);
        data = (Blob) map.get("data");
        assertTrue(Arrays.equals(content, data.getContent()));

        reopenDB();

        doc = db.getDocument("doc1");
        map = (Map<String, Object>) doc.get("map");
        assertNotNull(map);
        assertTrue(map.get("data") instanceof Blob);
        data = (Blob) map.get("data");
        assertTrue(Arrays.equals(content, data.getContent()));
    }

//    @Test
//    public void testUpdateNestedMap() {
//        // original
//        {
//            doc.set("name", "Scott");
//            Map<String, Object> original = new HashMap<>();
//            original.put("city", "REDWOOD CITY");
//            doc.set("address", original);
//            doc.save();
//        }
//
//        // update one
//        Document doc1;
//        Map<String, Object> address1;
//        Database copyOfDB1 = db.copy();
//        try {
//            doc1 = copyOfDB1.getDocument("doc1");
//            address1 = (Map<String, Object>) doc1.get("address");
//            address1.put("city", "MOUNTAIN VIEW");
//            doc1.set("address", address1);
//
//            address1 = (Map<String, Object>) doc1.get("address");
//            address1.put("city", "SAN JOSE");
//            doc1.save();
//        } finally {
//            copyOfDB1.close();
//        }
//        Map<String, Object> reget = (Map<String, Object>) doc1.get("address");
//        Log.e(TAG, "reget -> " + reget);
//
//
//        // update two
//        Database copyOfDB2 = db.copy();
//        try {
//            Document doc2 = copyOfDB2.getDocument("doc1");
//            Map<String, Object> address2 = (Map<String, Object>) doc2.get("address");
//            address2.put("city", "REDWOOD CITY");
//            address2.put("zip", "94065");
//            //doc2.set("address", address2);
//            doc2.save();
//        } finally {
//            copyOfDB2.close();
//        }
//
//        reget = (Map<String, Object>) doc1.get("address");
//        Log.e(TAG, "address1 -> " + address1);
//        Log.e(TAG, "reget -> " + reget);
//    }

    @Test
    public void testCrashWithBlob() throws CouchbaseLiteException, IOException {
        DatabaseOptions options = new DatabaseOptions();
        options.setDirectory(dir);
        Database db1 = new Database("abc", options);
        Document doc1 = db1.getDocument("doc1");

        byte[] content = "12345".getBytes();
        Blob data = new Blob("text/plain", content);
        doc1.set("data", data);
        doc1.save();
        db1.close();
    }

    @Test
    public void testSetProperties() throws Exception {
        loadJSONResource("names_100.json");
        assertEquals(100, db.internal().getDocumentCount());
    }
}