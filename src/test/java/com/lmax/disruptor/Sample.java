package com.lmax.disruptor;

import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class Sample {

    public static void main(String[] args) {
        Disruptor<ValueEvent> disruptor = new Disruptor<ValueEvent>(ValueEvent.EVENT_FACTORY,
                1024*1024*16 , DaemonThreadFactory.INSTANCE);

        final EventHandler<ValueEvent> handler1 = new EventHandler<ValueEvent>() {
            // event will eventually be recycled by the Disruptor after it wraps
            public void onEvent(final ValueEvent event, final long sequence, final boolean endOfBatch) throws Exception {
                System.out.println("handler1:  Sequence: " + sequence + "   ValueEvent: " + event.getValue());
            }
        };
      final EventHandler<ValueEvent> handler2 = new EventHandler<ValueEvent>() {
          // event will eventually be recycled by the Disruptor after it wraps
          public void onEvent(final ValueEvent event, final long sequence, final boolean endOfBatch) throws Exception {
              System.out.println("handler2:  Sequence: " + sequence + "   ValueEvent: " + event.getValue());
          }
      };
//      disruptor.handleEventsWith(handler1, handler2);
        disruptor.handleEventsWith(handler1);
        RingBuffer<ValueEvent> ringBuffer = disruptor.start();

        int bufferSize = ringBuffer.getBufferSize();
        System.out.println("bufferSize =  " + bufferSize);

        long start = System.currentTimeMillis();
        for (long i = 0; i < 1024*1024; i++) {
            long seq = ringBuffer.next();
            try {
                String uuid = String.valueOf(i);
                ValueEvent valueEvent = ringBuffer.get(seq);
                valueEvent.setValue(uuid);
            } finally {
                ringBuffer.publish(seq);
            }
        }
        disruptor.shutdown();
        System.out.println("总耗时："+String.valueOf(System.currentTimeMillis()-start) +" ms");
    }

    public static final class ValueEvent {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public final static EventFactory<ValueEvent> EVENT_FACTORY = new EventFactory<ValueEvent>() {
            public ValueEvent newInstance() {
                return new ValueEvent();
            }
        };
    }



}
