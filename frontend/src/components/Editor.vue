<template>
  <div>
    <ckeditor
      :editor="editor"
      :config="editorConfig"
      label="textLabel"
      tag-name="textarea"
      v-model="localVal"
    ></ckeditor>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import ClassicEditor from '@ckeditor/ckeditor5-editor-classic/src/classiceditor';
import EssentialsPlugin from '@ckeditor/ckeditor5-essentials/src/essentials';
import BoldPlugin from '@ckeditor/ckeditor5-basic-styles/src/bold';
import ItalicPlugin from '@ckeditor/ckeditor5-basic-styles/src/italic';
import LinkPlugin from '@ckeditor/ckeditor5-link/src/link';
import ParagraphPlugin from '@ckeditor/ckeditor5-paragraph/src/paragraph';
import AutoformatPlugin from '@ckeditor/ckeditor5-autoformat/src/autoformat.js';
import BlockQuotePlugin from '@ckeditor/ckeditor5-block-quote/src/blockquote.js';
import CodeBlockPlugin from '@ckeditor/ckeditor5-code-block/src/codeblock.js';
import CKFinderPlugin from '@ckeditor/ckeditor5-ckfinder/src/ckfinder.js';
import CKFinderUploadAdapterPlugin from '@ckeditor/ckeditor5-adapter-ckfinder/src/uploadadapter.js';
import HeadingPlugin from '@ckeditor/ckeditor5-heading/src/heading.js';
import ImagePlugin from '@ckeditor/ckeditor5-image/src/image.js';
import ImageCaptionPlugin from '@ckeditor/ckeditor5-image/src/imagecaption.js';
import ImageStylePlugin from '@ckeditor/ckeditor5-image/src/imagestyle.js';
import ImageToolbarPlugin from '@ckeditor/ckeditor5-image/src/imagetoolbar.js';
import ImageUploadPlugin from '@ckeditor/ckeditor5-image/src/imageupload.js';
import IndentPlugin from '@ckeditor/ckeditor5-indent/src/indent.js';
import ListPlugin from '@ckeditor/ckeditor5-list/src/list.js';
import MathTypePlugin from '@wiris/mathtype-ckeditor5';
import MediaEmbedPlugin from '@ckeditor/ckeditor5-media-embed/src/mediaembed.js';
import PasteFromOfficePlugin from '@ckeditor/ckeditor5-paste-from-office/src/pastefromoffice';
import TablePlugin from '@ckeditor/ckeditor5-table/src/table.js';
import TableToolbarPlugin from '@ckeditor/ckeditor5-table/src/tabletoolbar.js';
import TextTransformationPlugin from '@ckeditor/ckeditor5-typing/src/texttransformation.js';

export default {
  name: 'Editor',
  props: {
    textVal: String
  },
  data() {
    return {
      localVal: '',
      editor: ClassicEditor,
      editorConfig: {
        plugins: [
          EssentialsPlugin,
          BoldPlugin,
          ItalicPlugin,
          LinkPlugin,
          ParagraphPlugin,
          HeadingPlugin,
          AutoformatPlugin,
          BlockQuotePlugin,
          CodeBlockPlugin,
          CKFinderPlugin,
          CKFinderUploadAdapterPlugin,
          ImagePlugin,
          ImageCaptionPlugin,
          ImageStylePlugin,
          ImageToolbarPlugin,
          ImageUploadPlugin,
          IndentPlugin,
          ListPlugin,
          MathTypePlugin,
          MediaEmbedPlugin,
          PasteFromOfficePlugin,
          TablePlugin,
          TableToolbarPlugin,
          TextTransformationPlugin
        ],
        toolbar: {
          items: [
            'heading',
            '|',
            'bold',
            'italic',
            'link',
            'bulletedList',
            'numberedList',
            '|',
            'indent',
            'outdent',
            '|',
            'CKFinder',
            'imageUpload',
            'blockQuote',
            'codeBlock',
            'insertTable',
            'mediaEmbed',
            'undo',
            'redo',
            'MathType',
            'ChemType'
          ]
        },
        image: {
          toolbar: [
            'imageTextAlternative',
            'imageStyle:full',
            'imageStyle:side'
          ]
        },
        table: {
          contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells']
        },
        licenseKey: ''
      }
    };
  },
  created() {
    this.localVal = this.textVal;

    this.$watch('localVal', textVal => {
      this.$emit('input', textVal);
    });
  }
};
</script>
