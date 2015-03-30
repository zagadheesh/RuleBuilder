/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import beans.Node;

/**
 *
 * @author jagadeesh.t
 */
public class Stack1 {
    
    

        private Node[] a;
        private int top, m;

        public Stack1(int max) {
            m = max;
            a = new Node[m];
            top = -1;
        }

        public void push(Node key) {
            a[++top] = key;
        }

        public Node pop() {
            return (a[top--]);
        }

        public boolean isEmpty() {
            return (top == -1);
        }
    
    
}
