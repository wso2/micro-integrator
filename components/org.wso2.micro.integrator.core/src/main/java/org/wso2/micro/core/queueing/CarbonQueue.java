/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.micro.core.queueing;

import java.util.List;

/**
 * represents a generic queue that can be used by the carbon platform.
 *
 * @param <T> the type of objects stored in this queue.
 */
@SuppressWarnings("unused")
public interface CarbonQueue<T> {

    /**
     * Retrieves, but does not remove, the head of this queue.
     *
     * @return the head of this queue.
     * @throws QueueEmptyException when this queue is empty.
     */
    T peek() throws QueueEmptyException;

    /**
     * Retrieves, but does not remove, the given number of items from the top, which also includes
     * the head of this queue.
     *
     * @param count the number of items to return.
     *
     * @return the given number of items from the top, which also includes the head of this queue
     * @throws QueueEmptyException when this queue is empty.
     */
    List<T> peek(int count) throws QueueEmptyException;

    /**
     * Retrieves and removes, the head of this queue.
     *
     * @return the head of this queue.
     * @throws QueueEmptyException when this queue is empty.
     */
    T pop() throws QueueEmptyException;

    /**
     * Retrieves and removes, the given number of items from the top, which also includes the head
     * of this queue.
     *
     * @param count the number of items to return.
     *
     * @return the given number of items from the top, which also includes the head of this queue.
     * @throws QueueEmptyException when this queue is empty.
     */
    List<T> pop(int count) throws QueueEmptyException;

    /**
     * Adds the given element onto the queue.
     * @param element the element to add.
     */
    void push(T element);

    /**
     * Adds the given elements onto the queue.
     * @param elements the elements to add.
     */
    void push(List<T> elements);

    /**
     * Method to obtain the size of this queue.
     * @return the size of this queue.
     */
    int size();

    /**
     * Method to test whether this queue is empty.
     * @return true if the queue is empty or false if not.
     */
    boolean isEmpty();

    /**
     * Method to clear this queue.
     */
    void clear();

    /**
     * This method returns the index of the given element on this queue.
     *
     * @param element the element.
     *
     * @return the index of the given element.
     * @throws QueueEmptyException when this queue is empty.
     */
    int indexOf(T element) throws QueueEmptyException;

    /**
     * Method to retrieve the element at the given index of this queue.
     *
     * @param index the index of the element.
     *
     * @return the element.
     */
    T get(int index);

}
