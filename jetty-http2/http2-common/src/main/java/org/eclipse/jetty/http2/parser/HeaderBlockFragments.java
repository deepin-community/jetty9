//
//  ========================================================================
//  Copyright (c) 1995-2022 Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.http2.parser;

import java.nio.ByteBuffer;

import org.eclipse.jetty.http2.frames.PriorityFrame;

public class HeaderBlockFragments
{
    private final int maxCapacity;
    private PriorityFrame priorityFrame;
    private int streamId;
    private boolean endStream;
    private ByteBuffer storage;

    public HeaderBlockFragments(int maxCapacity)
    {
        this.maxCapacity = maxCapacity;
    }

    void reset()
    {
        priorityFrame = null;
        streamId = 0;
        endStream = false;
        storage = null;
    }

    public boolean storeFragment(ByteBuffer fragment, int length, boolean last)
    {
        if (storage == null)
        {
            if (length > maxCapacity)
                return false;
            int capacity = last ? length : length * 2;
            storage = ByteBuffer.allocate(capacity);
        }

        // Grow the storage if necessary.
        if (storage.remaining() < length)
        {
            if (storage.position() + length > maxCapacity)
                return false;
            int space = last ? length : length * 2;
            int capacity = storage.position() + space;
            ByteBuffer newStorage = ByteBuffer.allocate(capacity);
            storage.flip();
            newStorage.put(storage);
            storage = newStorage;
        }

        // Copy the fragment into the storage.
        int limit = fragment.limit();
        fragment.limit(fragment.position() + length);
        storage.put(fragment);
        fragment.limit(limit);
        return true;
    }

    public PriorityFrame getPriorityFrame()
    {
        return priorityFrame;
    }

    public void setPriorityFrame(PriorityFrame priorityFrame)
    {
        this.priorityFrame = priorityFrame;
    }

    public boolean isEndStream()
    {
        return endStream;
    }

    public void setEndStream(boolean endStream)
    {
        this.endStream = endStream;
    }

    public ByteBuffer complete()
    {
        storage.flip();
        return storage;
    }

    public int getStreamId()
    {
        return streamId;
    }

    public void setStreamId(int streamId)
    {
        this.streamId = streamId;
    }
}
