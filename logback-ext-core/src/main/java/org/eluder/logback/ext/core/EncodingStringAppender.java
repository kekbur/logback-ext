package org.eluder.logback.ext.core;

/*
 * #[license]
 * logback-ext-core
 * %%
 * Copyright (C) 2014 - 2015 Tapio Rautonen
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * %[license]
 */

import ch.qos.logback.core.Context;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.DeferredProcessingAware;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;

public abstract class EncodingStringAppender<E extends DeferredProcessingAware, P> extends UnsynchronizedAppenderBase<E> {

    protected final ReentrantLock lock = new ReentrantLock(true);

    private Charset charset = Charset.forName("UTF-8");
    private boolean binary = false;
    private Encoder<E> encoder;
    private PayloadConverter<P> converter;
    private OutputStream stream;

    public final void setCharset(Charset charset) {
        if (encoder instanceof LayoutWrappingEncoder) {
            ((LayoutWrappingEncoder<?>) encoder).setCharset(charset);
        } else if (encoder instanceof CharacterEncoder) {
            ((CharacterEncoder<?>) encoder).setCharset(charset);
        }
        this.charset = charset;
    }

    public final void setBinary(boolean binary) {
        if (binary) {
            addInfo(format("Appender '%s' is set to binary mode, events are converted to Base64 strings", getName()));
        }
        this.binary = binary;
    }

    public final void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
        setContext(context);
        setCharset(charset);
    }

    public final void setLayout(Layout<E> layout) {
        LayoutWrappingEncoder<E> enc = new LayoutWrappingEncoder<>();
        enc.setLayout(layout);
        setEncoder(enc);
    }

    public final void setConverter(PayloadConverter<P> converter) {
        this.converter = converter;
    }

    @Override
    public void setContext(Context context) {
        if (encoder != null) {
            encoder.setContext(context);
        }
        super.setContext(context);
    }

    protected final Charset getCharset() {
        return charset;
    }

    protected final boolean isBinary() {
        return binary;
    }

    protected final Encoder<E> getEncoder() {
        return encoder;
    }

    @Override
    public void start() {
        if (encoder == null) {
            addError(format("Encoder not set for appender '%s'", getName()));
            return;
        }
        if (converter == null) {
            addError(format("Converter not set for appender '%s'", getName()));
            return;
        }
        lock.lock();
        try {
            encoder.start();
            super.start();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void stop() {
        lock.lock();
        try {
            super.stop();
            if (encoder != null) {
                encoder.stop();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void append(E event) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        encode(event, stream);
        doHandle(event, convert(stream.toByteArray()));
    }

    private void encode(E event, ByteArrayOutputStream stream) {
        lock.lock();
        try {
            encoderInit(stream);
            try {
                doEncode(event);
            } finally {
                encoderClose();
            }
        } finally {
            lock.unlock();
        }
    }

    protected abstract void handle(E event, P encoded) throws Exception;

    protected P convert(byte[] payload) {
        return converter.convert(payload);
    }

    protected void doHandle(E event, P encoded) {
        try {
            if (encoded != null) {
                handle(event, encoded);
            }
        } catch (Exception ex) {
            this.started = false;
            addError(format("Failed to handle logging event for '%s'", getName()), ex);
        }
    }

    protected void doEncode(E event) {
        try {
            stream.write(encoder.encode(event));
        } catch (IOException ex) {
            this.started = false;
            addError(format("Failed to encode logging event for appender '%s'", getName()), ex);
        }
    }

    protected void encoderInit(ByteArrayOutputStream stream) {
        if (this.stream == null) {
            this.stream = stream;
        } else {
            try {
                this.stream.close();
                this.stream = stream;
            } catch (IOException ex) {
                this.started = false;
                addError(format("Failed to initialize encoder for appender '%s'", getName()), ex);
            }
        }
    }

    protected void encoderClose() {
        try {
            stream.close();
        } catch (Exception ex) {
            this.started = false;
            addError(format("Failed to close encoder for appender '%s'", getName()), ex);
        }
    }
}
