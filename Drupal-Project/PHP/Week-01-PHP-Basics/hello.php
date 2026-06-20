<?php
// A Drupal render array (this builds a page element)
$element = [
    '#type' => 'markup',
    '#markup' => '<p>Hello World</p>',
    '#prefix' => '<div class="greeting">',
    '#suffix' => '</div>',
];

// A Drupal form element
$form['name'] = [
    '#type' => 'textfield',
    '#title' => 'Your Name',
    '#required' => TRUE,
    '#default_value' => 'Sheetal',
];